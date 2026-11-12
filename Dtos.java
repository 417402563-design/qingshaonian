package cn.edu.sdua.qingshaonian.dto;

import cn.edu.sdua.qingshaonian.entity.User;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * API 请求/响应 DTO 集合
 */
public class Dtos {

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password,
                                  String nickname, String phone, String school, String grade, String className) {
    }

    public record CounselorRequest(@NotBlank String username, @NotBlank String password,
                                   String nickname, String phone, String title,
                                   String specialty, String licenseNo, String intro, String permissions) {
    }

    public record UserStatusRequest(String status, String password, String permissions) {
    }

    public record ChatRequest(Long sessionId, @NotBlank String content) {
    }

    public record ChatReply(Long sessionId, Long messageId, String reply, String categoryCode,
                            String emotionTag, String riskLevel, String source, Boolean isFallback,
                            List<String> suggestions) {
    }

    public record CaseProgressRequest(String content, String status, String nextFollowUpDate) {
    }

    public record SurveySubmitRequest(@NotBlank List<Integer> answers) {
    }

    public record PasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {
    }

    /** 去除密码后的用户信息 */
    public static User sanitize(User user) {
        user.setPassword(null);
        return user;
    }
}
