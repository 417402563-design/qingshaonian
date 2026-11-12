package cn.edu.sdua.qingshaonian.config;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.entity.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证 + 角色权限拦截器
 * 路径前缀约定：/api/admin/**、/api/counselor/**、/api/student/** 分别限对应角色访问
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_ROLE = "currentRole";
    public static final String ATTR_USERNAME = "currentUsername";

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new ApiException(401, "未登录或令牌缺失");
        }
        Claims claims;
        try {
            claims = jwtProvider.parse(auth.substring(7));
        } catch (Exception e) {
            throw new ApiException(401, "登录状态已失效，请重新登录");
        }
        String role = claims.get("role", String.class);
        request.setAttribute(ATTR_USER_ID, Long.valueOf(claims.getSubject()));
        request.setAttribute(ATTR_ROLE, role);
        request.setAttribute(ATTR_USERNAME, claims.get("username", String.class));

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin/") && !Role.ADMIN.equals(role)) {
            throw new ApiException(403, "无管理员权限");
        }
        if (uri.startsWith("/api/counselor/") && !Role.COUNSELOR.equals(role)) {
            throw new ApiException(403, "无咨询师权限");
        }
        if (uri.startsWith("/api/student/") && !Role.STUDENT.equals(role)) {
            throw new ApiException(403, "无学生权限");
        }
        return true;
    }
}
