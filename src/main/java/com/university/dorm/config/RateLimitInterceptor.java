package com.university.dorm.config;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.university.dorm.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitInterceptor implements HandlerInterceptor {

    // Key: IP_Minute, Value: Count
    // Expire after 2 minutes to allow overlap cleanup
    private static final TimedCache<String, Integer> REQUEST_COUNTS = CacheUtil.newTimedCache(2 * 60 * 1000);
    private static final int MAX_REQUESTS_PER_MINUTE = 300; // Increased limit (5 req/sec avg)

    static {
        // Start the scheduled task to prune expired entries every 30 seconds
        REQUEST_COUNTS.schedulePrune(30000);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String ip = getClientIp(request);
        // Use Fixed Window algorithm: Key = IP + "_" + CurrentMinute
        long currentMinute = System.currentTimeMillis() / 60000;
        String key = ip + "_" + currentMinute;
        
        Integer count = REQUEST_COUNTS.get(key, false);

        if (count == null) {
            count = 0;
        }

        if (count >= MAX_REQUESTS_PER_MINUTE) {
            System.out.println("Rate limit exceeded for IP: " + ip);
            returnJson(response, Result.error("请求过于频繁，请稍后再试"));
            return false;
        }

        REQUEST_COUNTS.put(key, count + 1);
        return true;
    }

    private void returnJson(HttpServletResponse response, Object json) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(new ObjectMapper().writeValueAsString(json));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
