package cn.edu.sdua.qingshaonian.controller;

import cn.edu.sdua.qingshaonian.config.CurrentUser;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.dto.Result;
import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import cn.edu.sdua.qingshaonian.entity.ChatSession;
import cn.edu.sdua.qingshaonian.service.ChatService;
import cn.edu.sdua.qingshaonian.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生端接口：智能咨询（双路径应答）与自我探索测评
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final ChatService chatService;
    private final SurveyService surveyService;
    private final CurrentUser currentUser;

    @PostMapping("/chat/ask")
    public Result<Dtos.ChatReply> ask(@Valid @RequestBody Dtos.ChatRequest req) {
        return Result.ok(chatService.ask(currentUser.id(), req));
    }

    @GetMapping("/chat/sessions")
    public Result<List<ChatSession>> sessions() {
        return Result.ok(chatService.mySessions(currentUser.id()));
    }

    @GetMapping("/chat/sessions/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id) {
        return Result.ok(chatService.messages(currentUser.id(), id));
    }

    /** 主动转人工咨询师介入 */
    @PostMapping("/chat/sessions/{id}/escalate")
    public Result<Long> escalate(@PathVariable Long id) {
        return Result.ok(chatService.escalate(currentUser.id(), id));
    }

    @GetMapping("/surveys")
    public Result<List<Map<String, Object>>> surveys() {
        return Result.ok(surveyService.list());
    }

    @PostMapping("/surveys/{id}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id, @Valid @RequestBody Dtos.SurveySubmitRequest req) {
        return Result.ok(surveyService.submit(id, req.answers()));
    }
}
