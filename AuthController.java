package cn.edu.sdua.qingshaonian.controller;

import cn.edu.sdua.qingshaonian.config.CurrentUser;
import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.dto.Result;
import cn.edu.sdua.qingshaonian.entity.Role;
import cn.edu.sdua.qingshaonian.entity.User;
import cn.edu.sdua.qingshaonian.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口（登录/注册公开，其余需携带 JWT）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody Dtos.LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody Dtos.RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    /** 管理员创建咨询师账号（仅 ADMIN 角色可调用） */
    @PostMapping("/counselor")
    public Result<User> createCounselor(@Valid @RequestBody Dtos.CounselorRequest req) {
        if (!Role.ADMIN.equals(currentUser.role())) {
            throw new ApiException(403, "仅管理员可创建咨询师账号");
        }
        return Result.ok(authService.createCounselor(req));
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(authService.me(currentUser.id()));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, String> fields) {
        return Result.ok(authService.updateProfile(currentUser.id(), fields));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody Dtos.PasswordRequest req) {
        authService.changePassword(currentUser.id(), req);
        return Result.ok();
    }
}
