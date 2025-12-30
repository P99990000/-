package com.university.dorm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Bean
    public AdminInterceptor adminInterceptor() {
        return new AdminInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rate Limit Interceptor (Applied to all API requests)
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**");

        // Auth Interceptor (Basic Auth)
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/mock/**", "/api/big-screen/**", "/api/admin/ai-report", "/api/report/**");
        
        // Admin Interceptor (Role check for Admin APIs)
        registry.addInterceptor(adminInterceptor())
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/ai-report"); // If this is public/shared, otherwise include it
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the file system uploads directory
        // System.getProperty("user.dir") returns the project root
        String uploadPath = "file:" + System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
