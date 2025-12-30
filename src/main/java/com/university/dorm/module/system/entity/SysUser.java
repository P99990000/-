package com.university.dorm.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password; // BCrypt encrypted

    @TableField(exist = false)
    private String salt; // Not used with BCrypt, marking as non-database field

    private String role; // admin, inspector, student

    private String realName;

    private Integer status; // 1: active, 0: disabled

    private LocalDateTime createTime;
}
