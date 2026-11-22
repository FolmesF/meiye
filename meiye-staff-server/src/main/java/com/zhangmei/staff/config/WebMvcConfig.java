package com.zhangmei.staff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangmei.staff.common.R;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.security.StaffContext;
import com.zhangmei.staff.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RbacInterceptor(authService, objectMapper))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/internal/**");
    }

    @RequiredArgsConstructor
    static class RbacInterceptor implements HandlerInterceptor {

        private final AuthService authService;
        private final ObjectMapper objectMapper;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                write(response, 401, "请先登录");
                return false;
            }
            Long employeeId = authService.resolveEmployee(header.substring("Bearer ".length()).trim());
            if (employeeId == null) {
                write(response, 401, "登录已失效");
                return false;
            }
            Set<String> permissions = authService.permissionCodes(employeeId);
            StaffContext.set(employeeId, permissions);
            if (handler instanceof HandlerMethod handlerMethod) {
                RequirePermission required = handlerMethod.getMethodAnnotation(RequirePermission.class);
                if (required != null && !permissions.contains(required.value())) {
                    write(response, 403, "无权限");
                    return false;
                }
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            StaffContext.clear();
        }

        private void write(HttpServletResponse response, int code, String message) throws Exception {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(code, message)));
        }
    }
}
