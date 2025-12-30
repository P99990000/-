package com.university.dorm.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检查明细表
 */
@Data
@TableName("inspection_detail")
public class InspectionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的主记录ID
     */
    private Long recordId;

    /**
     * 关联的检查项ID
     */
    private Long itemId;

    /**
     * 实际得分
     */
    private BigDecimal score;

    /**
     * 扣分说明
     */
    private String deductionReason;

    /**
     * 现场照片URL
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
