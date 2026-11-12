package cn.edu.sdua.qingshaonian.config;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 读取当前登录用户上下文（由 AuthInterceptor 写入 request 属性）
 */
@Component
public class CurrentUser {

    public Long id() {
        Object id = request().getAttribute(AuthInterceptor.ATTR_USER_ID);
        if (id == null) {
            throw new ApiException(401, "未登录");
        }
        return (Long) id;
    }

    public String role() {
        return (String) request().getAttribute(AuthInterceptor.ATTR_ROLE);
    }

    public String username() {
        return (String) request().getAttribute(AuthInterceptor.ATTR_USERNAME);
    }

    private HttpServletRequest request() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
