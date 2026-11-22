package com.zhangmei.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangmei.booking.common.R;
import com.zhangmei.booking.common.UserContext;
import com.zhangmei.booking.redis.LoginTokenStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginTokenStore loginTokenStore;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(loginTokenStore, objectMapper))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/availability/**", "/api/catalog/**", "/api/internal/**");
    }

    @RequiredArgsConstructor
    static class LoginInterceptor implements HandlerInterceptor {

        private final LoginTokenStore loginTokenStore;
        private final ObjectMapper objectMapper;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                writeUnauthorized(response);
                return false;
            }
            Long userId = loginTokenStore.getUserId(header.substring("Bearer ".length()).trim());
            if (userId == null) {
                writeUnauthorized(response);
                return false;
            }
            UserContext.set(userId);
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            UserContext.clear();
        }

        private void writeUnauthorized(HttpServletResponse response) throws Exception {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, "请先登录")));
        }
    }
}
