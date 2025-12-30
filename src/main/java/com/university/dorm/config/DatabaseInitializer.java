package com.university.dorm.config;

import cn.hutool.crypto.digest.BCrypt;
import com.university.dorm.module.system.entity.SysUser;
import com.university.dorm.module.system.mapper.LoginLogMapper;
import com.university.dorm.module.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public void run(String... args) throws Exception {
        // Initialize tables
        try {
            sysUserMapper.createTable();
            loginLogMapper.createTable();
            
            // Initialize default admin if not exists
            if (sysUserMapper.selectCount(null) == 0) {
                // Admin
                SysUser admin = new SysUser();
                admin.setUsername("admin");
                admin.setPassword(BCrypt.hashpw("admin123")); // Default password
                admin.setRole("admin");
                admin.setRealName("系统管理员");
                admin.setStatus(1);
                admin.setCreateTime(LocalDateTime.now());
                sysUserMapper.insert(admin);
                
                // Inspector
                SysUser inspector = new SysUser();
                inspector.setUsername("inspector");
                inspector.setPassword(BCrypt.hashpw("123456"));
                inspector.setRole("inspector");
                inspector.setRealName("宿管员");
                inspector.setStatus(1);
                inspector.setCreateTime(LocalDateTime.now());
                sysUserMapper.insert(inspector);
                
                System.out.println("Initialized default users: admin/admin123, inspector/123456");
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed (tables might already exist or permission denied): " + e.getMessage());
        }
    }
}
