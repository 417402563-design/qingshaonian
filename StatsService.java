package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import cn.edu.sdua.qingshaonian.entity.ChatSession;
import cn.edu.sdua.qingshaonian.entity.Role;
import cn.edu.sdua.qingshaonian.repository.ChatMessageRepository;
import cn.edu.sdua.qingshaonian.repository.ChatSessionRepository;
import cn.edu.sdua.qingshaonian.repository.InterventionCaseRepository;
import cn.edu.sdua.qingshaonian.repository.KnowledgeItemRepository;
import cn.edu.sdua.qingshaonian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 后台问答记录可视化与关键词热词分析服务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    /** 内置青少年心理领域热词词典（避免外部分词依赖，内地环境可直接运行） */
    private static final List<String> HOT_WORDS = List.of(
            "压力", "焦虑", "失眠", "熬夜", "考试", "高考", "中考", "成绩", "排名", "作业",
            "升学", "厌学", "逃学", "手机", "网络", "游戏", "短视频", "早恋", "恋爱", "表白",
            "分手", "单亲", "留守", "霸凌", "孤立", "排挤", "同学", "朋友", "室友", "人际",
            "社交", "恐惧", "崩溃", "委屈", "烦躁", "愤怒", "吵架", "叛逆", "代沟", "批评",
            "打骂", "期待", "自卑", "敏感", "内向", "孤独", "迷茫", "自尊", "情绪", "心情",
            "低落", "难过", "伤心", "哭泣", "想哭", "绝望", "无助", "自残", "伤害", "抑郁",
            "心理", "咨询", "倾诉", "倾听", "减压", "放松", "呼吸", "运动", "兴趣", "梦想",
            "目标", "未来", "自信", "勇气", "坚持", "努力", "内耗", "比较", "转学", "适应");

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final InterventionCaseRepository caseRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final DeepSeekService deepSeekService;

    /** 问答记录分页（以学生提问为主体，附所属会话信息） */
    public Map<String, Object> qaRecords(int page, int size, LocalDateTime start, LocalDateTime end, String category) {
        Page<ChatMessage> qaPage = messageRepository.findByRoleAndCreatedAtBetween("user", start, end,
                PageRequest.of(page, size));
        List<ChatSession> sessions = sessionRepository.findAll();
        Map<Long, ChatSession> sessionMap = new HashMap<>();
        sessions.forEach(s -> sessionMap.put(s.getId(), s));
        List<Map<String, Object>> rows = qaPage.getContent().stream()
                .filter(m -> category == null || category.isBlank() || category.equals(m.getCategoryCode()))
                .map(m -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", m.getId());
                    row.put("createdAt", m.getCreatedAt());
                    row.put("content", m.getContent());
                    row.put("categoryCode", m.getCategoryCode());
                    row.put("emotionTag", m.getEmotionTag());
                    row.put("riskFlag", m.getRiskFlag());
                    ChatSession s = sessionMap.get(m.getSessionId());
                    row.put("sessionId", m.getSessionId());
                    row.put("studentId", s == null ? null : s.getUserId());
                    row.put("sessionStatus", s == null ? null : s.getStatus());
                    return row;
                }).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("content", rows);
        result.put("total", qaPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /** 可视化分析：趋势折线 + 分类分布 + 情绪分布 + 热词词云 */
    public Map<String, Object> analytics(LocalDateTime start, LocalDateTime end) {
        List<ChatMessage> questions = messageRepository.findByRoleAndCreatedAtBetween("user", start, end);

        // 每日问答量趋势
        Map<String, Long> trend = new TreeMap<>();
        LocalDate cursor = start.toLocalDate();
        while (!cursor.isAfter(end.toLocalDate())) {
            trend.put(cursor.toString(), 0L);
            cursor = cursor.plusDays(1);
        }
        for (ChatMessage m : questions) {
            trend.merge(m.getCreatedAt().toLocalDate().toString(), 1L, Long::sum);
        }

        // 命中分类分布
        Map<String, Long> categoryDist = new LinkedHashMap<>();
        Map<String, Long> emotionDist = new LinkedHashMap<>();
        long riskCount = 0;
        for (ChatMessage m : questions) {
            categoryDist.merge(m.getCategoryCode() == null ? "未分类" : m.getCategoryCode(), 1L, Long::sum);
            emotionDist.merge(m.getEmotionTag() == null ? "平静" : m.getEmotionTag(), 1L, Long::sum);
            if (Boolean.TRUE.equals(m.getRiskFlag())) {
                riskCount++;
            }
        }

        // 热词词频统计
        Map<String, Long> freq = new HashMap<>();
        for (ChatMessage m : questions) {
            String text = m.getContent() == null ? "" : m.getContent();
            for (String w : HOT_WORDS) {
                int idx = 0;
                while ((idx = text.indexOf(w, idx)) >= 0) {
                    freq.merge(w, 1L, Long::sum);
                    idx += w.length();
                }
            }
        }
        List<Map<String, Object>> hotWords = freq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(40)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalQuestions", questions.size());
        result.put("riskCount", riskCount);
        result.put("trend", trend.entrySet().stream()
                .map(e -> Map.of("date", e.getKey(), "count", e.getValue())).toList());
        result.put("categoryDist", sortedDesc(categoryDist));
        result.put("emotionDist", sortedDesc(emotionDist));
        result.put("hotWords", hotWords);
        return result;
    }

    /** 数据统计看板总览 */
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT).size());
        result.put("counselorCount", userRepository.findByRoleOrderByCreatedAtDesc(Role.COUNSELOR).size());
        result.put("sessionCount", sessionRepository.count());
        result.put("questionCount", messageRepository.countByRole("user"));
        result.put("caseOpen", caseRepository.countByStatus("OPEN"));
        result.put("caseInProgress", caseRepository.countByStatus("IN_PROGRESS"));
        result.put("caseClosed", caseRepository.countByStatus("CLOSED"));
        result.put("knowledgeCount", knowledgeItemRepository.count());
        result.put("highRiskSessions", sessionRepository.findRiskySessions("NONE").stream()
                .filter(s -> "HIGH".equals(s.getRiskLevel())).count());
        return result;
    }

    /** 服务状态监控 */
    public Map<String, Object> serviceStatus() {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long maxMb = rt.maxMemory() / 1024 / 1024;

        List<ChatSession> sessions = sessionRepository.findAll();
        long activeToday = sessions.stream()
                .filter(s -> s.getUpdatedAt().toLocalDate().equals(LocalDate.now())).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("appStatus", "UP");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("memoryUsedMb", usedMb);
        result.put("memoryMaxMb", maxMb);
        result.put("dbStatus", userRepository.count() >= 0 ? "UP" : "DOWN");
        result.put("activeSessionsToday", activeToday);
        result.put("model", deepSeekService.metrics());
        return result;
    }

    private List<Map<String, Object>> sortedDesc(Map<String, Long> dist) {
        List<Map<String, Object>> list = new ArrayList<>();
        dist.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<String, Object>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    list.add(item);
                });
        return list;
    }
}
