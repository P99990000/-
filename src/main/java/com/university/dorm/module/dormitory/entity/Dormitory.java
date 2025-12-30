package com.university.dorm.module.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宿舍表
 */
@Data
@TableName("dormitory")
public class Dormitory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 宿舍长学号
     */
    private String managerStudentSn;

    /**
     * 床位容量
     */
    private Integer capacity;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
