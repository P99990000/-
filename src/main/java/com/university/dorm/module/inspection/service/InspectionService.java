package com.university.dorm.module.inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.university.dorm.module.admin.dto.BuildingStatsDTO;
import com.university.dorm.module.admin.dto.DormScoreDTO;
import com.university.dorm.module.inspection.dto.InspectionSubmitDTO;
import com.university.dorm.module.inspection.entity.InspectionRecord;

import com.university.dorm.module.inspection.dto.RectificationReviewDTO;
import com.university.dorm.module.inspection.dto.RectificationSubmitDTO;

import java.util.List;

public interface InspectionService extends IService<InspectionRecord> {
    
    /**
     * 获取各楼栋卫生统计数据
     * @return 统计列表
     */
    List<BuildingStatsDTO> getBuildingStats();

    /**
     * 获取所有宿舍最新评分
     * @return 评分列表
     */
    List<DormScoreDTO> getAllDormScores();

    /**
     * 根据宿舍ID查询检查记录（包含明细）
     * @param dormId 宿舍ID
     * @return 记录列表
     */
    List<InspectionRecord> getRecordsByDormId(Long dormId);

    /**
     * 提交卫生检查记录
     * @param dto 提交的数据
     * @return 是否成功
     */
    boolean submitRecord(InspectionSubmitDTO dto);

    /**
     * 提交整改
     */
    boolean submitRectification(RectificationSubmitDTO dto);

    /**
     * 审核整改
     */
    boolean reviewRectification(RectificationReviewDTO dto);

    /**
     * 获取待审核整改记录
     * @return 记录列表
     */
    List<InspectionRecord> getPendingRectifications();

    /**
     * 获取最近的检查记录
     * @param limit 限制条数
     * @return 记录列表
     */
    List<InspectionRecord> getRecentRecords(Integer limit);

    /**
     * 获取最高频扣分项
     * @return 扣分项列表
     */
    List<java.util.Map<String, Object>> getTopIssues();
}
