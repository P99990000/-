package com.university.dorm.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查记录主表
 */
@Data
@TableName("inspection_record")
public class InspectionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 受检宿舍ID
     */
    private Long dormId;

    /**
     * 检查人员姓名
     */
    private String inspectorName;

    /**
     * 总得分
     */
    private BigDecimal totalScore;

    /**
     * 检查日期
     */
    private LocalDate checkDate;

    /**
     * 整改意见/备注
     */
    private String remark;

    /**
     * 是否通报 (0:否, 1:是)
     */
    private Integer isNotice;

    /**
     * 现场照片URL
     */
    private String imageUrl;

    /**
     * 整改状态 (0:无, 1:待审核, 2:通过, 3:驳回, 4:需整改)
     */
    private Integer rectificationStatus;

    /**
     * 整改说明
     */
    private String rectificationDesc;

    /**
     * 整改照片URL
     */
    private String rectificationImageUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 检查明细列表 (非数据库字段)
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<InspectionDetail> details;

    /**
     * 楼栋名称 (非数据库字段)
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String buildingName;

    /**
     * 宿舍号 (非数据库字段)
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String roomNumber;
}
