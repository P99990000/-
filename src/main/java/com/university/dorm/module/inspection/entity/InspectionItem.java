package com.university.dorm.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检查项配置表
 */
@Data
@TableName("inspection_item")
public class InspectionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 检查项名称
     */
    private String itemName;

    /**
     * 满分值
     */
    private Integer maxScore;

    /**
     * 评分标准描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer isEnabled;

    /**
     * 排序优先级
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
