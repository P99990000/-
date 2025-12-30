package com.university.dorm.config;

import cn.hutool.jwt.JWTUtil;
import com.university.dorm.module.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final byte[] JWT_KEY = "dorm-system-secret-key-2025".getBytes(StandardCharsets.UTF_8);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        try {
            if (JWTUtil.verify(token, JWT_KEY)) {
                String role = (String) JWTUtil.parseToken(token).getPayload("role");
                if ("admin".equals(role)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Token invalid or role missing
        }

        response.setStatus(403); // Forbidden
        return false;
    }
}
