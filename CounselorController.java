package cn.edu.sdua.qingshaonian.controller;

import cn.edu.sdua.qingshaonian.config.CurrentUser;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.dto.Result;
import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import cn.edu.sdua.qingshaonian.entity.InterventionCase;
import cn.edu.sdua.qingshaonian.service.CounselorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 咨询师端接口：工作台总览、风险会话、案例干预（仅 COUNSELOR 角色，由 AuthInterceptor 保障）
 */
@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
public class CounselorController {

    private final CounselorService counselorService;
    private final CurrentUser currentUser;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(counselorService.dashboard(currentUser.id()));
    }

    @GetMapping("/sessions/risk")
    public Result<List<Map<String, Object>>> riskSessions() {
        return Result.ok(counselorService.riskSessions());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> sessionMessages(@PathVariable Long sessionId) {
        return Result.ok(counselorService.sessionMessages(sessionId));
    }

    @GetMapping("/cases")
    public Result<List<InterventionCase>> cases(@RequestParam(required = false) String status) {
        return Result.ok(counselorService.myCases(currentUser.id(), status));
    }

    @PostMapping("/cases")
    public Result<InterventionCase> saveCase(@RequestBody InterventionCase payload) {
        return Result.ok(counselorService.saveCase(currentUser.id(), payload));
    }

    /** 从风险会话直接立卷 */
    @PostMapping("/cases/from-session/{sessionId}")
    public Result<InterventionCase> createFromSession(@PathVariable Long sessionId) {
        return Result.ok(counselorService.createFromSession(currentUser.id(), sessionId));
    }

    @GetMapping("/cases/{id}")
    public Result<Map<String, Object>> caseDetail(@PathVariable Long id) {
        return Result.ok(counselorService.caseDetail(id));
    }

    @PutMapping("/cases/{id}/progress")
    public Result<InterventionCase> addProgress(@PathVariable Long id, @RequestBody Dtos.CaseProgressRequest req) {
        return Result.ok(counselorService.addProgress(currentUser.id(), id, req));
    }

    @GetMapping("/students")
    public Result<List<Map<String, Object>>> students() {
        return Result.ok(counselorService.students());
    }
}
