package com.university.dorm.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.dorm.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    @Select("SELECT count(*) FROM information_schema.tables WHERE table_name = 'sys_user'")
    int checkTableExists();
    
    @Update("CREATE TABLE IF NOT EXISTS sys_user (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) NOT NULL UNIQUE," +
            "password VARCHAR(100) NOT NULL," +
            "role VARCHAR(20) NOT NULL," +
            "real_name VARCHAR(50)," +
            "status INT DEFAULT 1," +
            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
    void createTable();
}
