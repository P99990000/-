package com.university.dorm.module.system.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.dorm.common.result.Result;
import com.university.dorm.module.system.dto.LoginRequest;
import com.university.dorm.module.system.dto.LoginResponse;
import com.university.dorm.module.system.entity.LoginLog;
import com.university.dorm.module.system.entity.SysUser;
import com.university.dorm.module.system.mapper.LoginLogMapper;
import com.university.dorm.module.system.mapper.SysUserMapper;
import com.university.dorm.module.student.entity.Student;
import com.university.dorm.module.student.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;
    
    private static final byte[] JWT_KEY = "dorm-system-secret-key-2025".getBytes(StandardCharsets.UTF_8);

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Generate captcha: width 100, height 40, code count 4, line count 20
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, 4, 20);
        
        // Store code in session
        request.getSession().setAttribute("CAPTCHA_CODE", lineCaptcha.getCode());
        request.getSession().setAttribute("CAPTCHA_EXPIRE", System.currentTimeMillis() + 60 * 1000); // 1 min expire
        
        // Write to response
        response.setContentType("image/png");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        lineCaptcha.write(response.getOutputStream());
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @javax.validation.Valid LoginRequest loginRequest, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        // 1. Verify Captcha
        HttpSession session = request.getSession();
        String sessionCode = (String) session.getAttribute("CAPTCHA_CODE");
        Long expireTime = (Long) session.getAttribute("CAPTCHA_EXPIRE");
        
        // Clear captcha to prevent reuse
        session.removeAttribute("CAPTCHA_CODE");
        session.removeAttribute("CAPTCHA_EXPIRE");
        
        if (sessionCode == null || expireTime == null || System.currentTimeMillis() > expireTime) {
            recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "验证码失效");
            return Result.error("验证码失效，请刷新重试");
        }
        
        if (!sessionCode.equalsIgnoreCase(loginRequest.getCaptcha())) {
            recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "验证码错误");
            return Result.error("验证码错误");
        }

        String role = loginRequest.getRole();
        if ("student".equals(role)) {
            // Student Login Logic - Fixed Account
            if ("student".equals(loginRequest.getUsername()) && "2025hniu".equals(loginRequest.getPassword())) {
                // Generate Token
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", -1L); // Dummy ID for generic student account
                payload.put("username", "student");
                payload.put("role", "student");
                payload.put("expire_time", System.currentTimeMillis() + 1000 * 60 * 60 * 24);

                String token = JWTUtil.createToken(payload, JWT_KEY);

                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken(token);
                loginResponse.setUsername("学生用户");
                loginResponse.setRole("student");

                recordLog(loginRequest.getUsername(), clientIp, userAgent, 1, "学生登录成功");
                return Result.success(loginResponse);
            } else {
                recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "学生账号或密码错误");
                return Result.error("账号或密码错误");
            }

        } else {
            // Admin/Inspector Login Logic (Existing)
            // 2. Find User
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, loginRequest.getUsername());
            SysUser user = sysUserMapper.selectOne(wrapper);

            if (user == null) {
                recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "用户不存在");
                return Result.error("用户名或密码错误");
            }
            
            if (user.getStatus() != 1) {
                recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "账号已禁用");
                return Result.error("账号已禁用，请联系管理员");
            }

            // 3. Verify Password
            if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
                recordLog(loginRequest.getUsername(), clientIp, userAgent, 0, "密码错误");
                return Result.error("用户名或密码错误");
            }
            
            // 4. Generate Token with Role
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.getId());
            payload.put("username", user.getUsername());
            payload.put("role", user.getRole()); // Add role to payload
            payload.put("expire_time", System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours
            
            String token = JWTUtil.createToken(payload, JWT_KEY);
            
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUsername(user.getUsername());
            loginResponse.setRole(user.getRole());
            
            recordLog(loginRequest.getUsername(), clientIp, userAgent, 1, "登录成功");
            
            return Result.success(loginResponse);
        }
    }
    
    private void recordLog(String username, String ip, String browser, Integer status, String message) {
        try {
            LoginLog log = new LoginLog();
            log.setUsername(username);
            log.setIp(ip);
            log.setBrowser(browser != null && browser.length() > 50 ? browser.substring(0, 50) : browser);
            log.setStatus(status);
            log.setMessage(message);
            log.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
