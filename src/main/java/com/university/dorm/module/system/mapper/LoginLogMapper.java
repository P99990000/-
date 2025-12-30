package com.university.dorm.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.dorm.module.system.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
    @Update("CREATE TABLE IF NOT EXISTS sys_login_log (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50)," +
            "ip VARCHAR(50)," +
            "location VARCHAR(100)," +
            "browser VARCHAR(50)," +
            "os VARCHAR(50)," +
            "status INT," +
            "message VARCHAR(255)," +
            "login_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
    void createTable();
}
