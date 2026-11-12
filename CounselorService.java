package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import cn.edu.sdua.qingshaonian.entity.ChatSession;
import cn.edu.sdua.qingshaonian.entity.InterventionCase;
import cn.edu.sdua.qingshaonian.entity.Role;
import cn.edu.sdua.qingshaonian.entity.User;
import cn.edu.sdua.qingshaonian.repository.ChatMessageRepository;
import cn.edu.sdua.qingshaonian.repository.ChatSessionRepository;
import cn.edu.sdua.qingshaonian.repository.InterventionCaseRepository;
import cn.edu.sdua.qingshaonian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 咨询师端服务：工作台总览、风险会话、案例干预与进展跟踪
 */
@Service
@RequiredArgsConstructor
public class CounselorService {

    private final InterventionCaseRepository caseRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public Map<String, Object> dashboard(Long counselorId) {
        List<InterventionCase> myCases = caseRepository.findByCounselorId(counselorId);
        long myTotal = myCases.size();
        long myActive = myCases.stream().filter(c -> !"CLOSED".equals(c.getStatus())).count();
        List<ChatSession> riskSessions = sessionRepository.findRiskySessions("NONE");
        long escalatedPending = riskSessions.stream().filter(s -> "ESCALATED".equals(s.getStatus())).count();
        long todayNew = caseRepository.findAll().stream()
                .filter(c -> c.getCreatedAt().toLocalDate().equals(LocalDate.now())).count();

        Map<String, Object> result = new HashMap<>();
        result.put("myCaseTotal", myTotal);
        result.put("myCaseActive", myActive);
        result.put("riskSessionTotal", riskSessions.size());
        result.put("escalatedPending", escalatedPending);
        result.put("todayNewCases", todayNew);
        result.put("recentCases", myCases.stream().limit(5).toList());
        return result;
    }

    /** 风险会话列表（附学生信息与风险消息摘录） */
    public List<Map<String, Object>> riskSessions() {
        return sessionRepository.findRiskySessions("NONE").stream().map(s -> {
            Map<String, Object> row = new HashMap<>();
            row.put("session", s);
            userRepository.findById(s.getUserId()).ifPresent(u -> {
                row.put("studentName", u.getNickname());
                row.put("school", u.getSchool());
                row.put("grade", u.getGrade());
                row.put("className", u.getClassName());
            });
            List<ChatMessage> risky = messageRepository.findBySessionIdOrderByIdAsc(s.getId()).stream()
                    .filter(m -> Boolean.TRUE.equals(m.getRiskFlag()) || "user".equals(m.getRole()))
                    .toList();
            row.put("excerpt", risky.isEmpty() ? "" : risky.get(risky.size() - 1).getContent());
            return row;
        }).toList();
    }

    public List<InterventionCase> myCases(Long counselorId, String status) {
        List<InterventionCase> list = status == null || status.isBlank()
                ? caseRepository.findByCounselorId(counselorId)
                : caseRepository.findByCounselorId(counselorId).stream().filter(c -> status.equals(c.getStatus())).toList();
        // 未分配案例（同院共享池）：OPEN 且无负责人
        if (status == null || "OPEN".equals(status)) {
            List<InterventionCase> pool = caseRepository.findByStatus("OPEN").stream()
                    .filter(c -> c.getCounselorId() == null).toList();
            return Stream.concat(list.stream(), pool.stream().filter(p -> list.stream().noneMatch(c -> c.getId().equals(p.getId())))).distinct().toList();
        }
        return list;
    }

    @Transactional
    public InterventionCase saveCase(Long counselorId, InterventionCase payload) {
        if (payload.getId() != null) {
            InterventionCase exist = caseRepository.findById(payload.getId())
                    .orElseThrow(() -> new ApiException("案例不存在"));
            exist.setStudentId(payload.getStudentId() == null ? exist.getStudentId() : payload.getStudentId());
            exist.setLevel(payload.getLevel() == null ? exist.getLevel() : payload.getLevel());
            exist.setProblemCategory(payload.getProblemCategory() == null ? exist.getProblemCategory() : payload.getProblemCategory());
            exist.setSummary(payload.getSummary() == null ? exist.getSummary() : payload.getSummary());
            exist.setPlan(payload.getPlan() == null ? exist.getPlan() : payload.getPlan());
            exist.setNextFollowUpDate(payload.getNextFollowUpDate() == null ? exist.getNextFollowUpDate() : payload.getNextFollowUpDate());
            if (exist.getCounselorId() == null) {
                exist.setCounselorId(counselorId);
            }
            if ("OPEN".equals(exist.getStatus())) {
                exist.setStatus("IN_PROGRESS");
            }
            exist.setUpdatedAt(LocalDateTime.now());
            return caseRepository.save(exist);
        }
        payload.setCounselorId(counselorId);
        payload.setStatus(payload.getStatus() == null ? "IN_PROGRESS" : payload.getStatus());
        return caseRepository.save(payload);
    }

    /** 追加一条进展记录（带时间戳），可同时更新状态与下次跟进日期 */
    @Transactional
    public InterventionCase addProgress(Long counselorId, Long caseId, Dtos.CaseProgressRequest req) {
        InterventionCase caseEntity = caseRepository.findById(caseId).orElseThrow(() -> new ApiException("案例不存在"));
        if (caseEntity.getCounselorId() == null) {
            caseEntity.setCounselorId(counselorId);
        }
        String stamp = LocalDate.now() + " ";
        caseEntity.setProgress((caseEntity.getProgress() == null ? "" : caseEntity.getProgress() + "\n")
                + stamp + (req.content() == null ? "" : req.content().trim()));
        if (req.status() != null && !req.status().isBlank()) {
            caseEntity.setStatus(req.status());
        }
        if (req.nextFollowUpDate() != null && !req.nextFollowUpDate().isBlank()) {
            caseEntity.setNextFollowUpDate(LocalDate.parse(req.nextFollowUpDate()));
        }
        caseEntity.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(caseEntity);
    }

    /** 咨询师可查看任意会话的完整对话（工作权限） */
    public List<ChatMessage> sessionMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByIdAsc(sessionId);
    }

    /** 从风险会话直接立卷干预 */
    @Transactional
    public InterventionCase createFromSession(Long counselorId, Long sessionId) {
        if (caseRepository.findBySourceSessionId(sessionId).isPresent()) {
            throw new ApiException("该会话已建立干预案例，请到案例列表中处理");
        }
        ChatSession session = sessionRepository.findById(sessionId).orElseThrow(() -> new ApiException("会话不存在"));
        InterventionCase caseEntity = new InterventionCase();
        caseEntity.setStudentId(session.getUserId());
        caseEntity.setCounselorId(counselorId);
        caseEntity.setLevel("HIGH".equals(session.getRiskLevel()) ? "危机" : "重点");
        caseEntity.setProblemCategory(session.getCategoryCode());
        caseEntity.setSummary("由风险会话转人工立卷。情绪标签：" + session.getEmotionTag() + "；会话：" + session.getTitle());
        caseEntity.setSourceSessionId(sessionId);
        caseEntity.setStatus("IN_PROGRESS");
        return caseRepository.save(caseEntity);
    }

    public Map<String, Object> caseDetail(Long caseId) {
        InterventionCase caseEntity = caseRepository.findById(caseId).orElseThrow(() -> new ApiException("案例不存在"));
        Map<String, Object> result = new HashMap<>();
        result.put("case", caseEntity);
        userRepository.findById(caseEntity.getStudentId()).ifPresent(u -> result.put("student", Dtos.sanitize(u)));
        if (caseEntity.getSourceSessionId() != null) {
            result.put("messages", messageRepository.findBySessionIdOrderByIdAsc(caseEntity.getSourceSessionId()));
        }
        return result;
    }

    /** 可指定的学生简表（新建案例下拉选择用） */
    public List<Map<String, Object>> students() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT).stream().map(u -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("nickname", u.getNickname());
            row.put("grade", u.getGrade());
            row.put("className", u.getClassName());
            row.put("school", u.getSchool());
            return row;
        }).toList();
    }
}
