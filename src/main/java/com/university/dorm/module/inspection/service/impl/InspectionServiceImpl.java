package com.university.dorm.module.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.university.dorm.module.inspection.entity.InspectionDetail;
import com.university.dorm.module.inspection.entity.InspectionRecord;
import com.university.dorm.module.inspection.mapper.InspectionDetailMapper;
import com.university.dorm.module.inspection.mapper.InspectionRecordMapper;
import com.university.dorm.module.inspection.service.InspectionService;
import com.university.dorm.module.dormitory.entity.Dormitory;
import com.university.dorm.module.dormitory.mapper.DormitoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.university.dorm.module.admin.dto.BuildingStatsDTO;
import com.university.dorm.module.admin.dto.DormScoreDTO;
import com.university.dorm.module.inspection.dto.InspectionSubmitDTO;
import com.university.dorm.module.inspection.dto.RectificationReviewDTO;
import com.university.dorm.module.inspection.dto.RectificationSubmitDTO;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class InspectionServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord> implements InspectionService {

    @Autowired
    private InspectionDetailMapper inspectionDetailMapper;

    @Autowired
    private DormitoryMapper dormitoryMapper;

    @Override
    public List<BuildingStatsDTO> getBuildingStats() {
        List<BuildingStatsDTO> stats = baseMapper.selectBuildingStats();
        
        // Calculate excellentRate and format data
        if (stats != null) {
            for (BuildingStatsDTO dto : stats) {
                if (dto.getTotalChecks() != null && dto.getTotalChecks() > 0) {
                    BigDecimal rate = BigDecimal.valueOf(dto.getExcellentCount())
                            .divide(BigDecimal.valueOf(dto.getTotalChecks()), 2, RoundingMode.HALF_UP);
                    dto.setExcellentRate(rate);
                    
                    // Round avgScore to 1 decimal place
                    if (dto.getAvgScore() != null) {
                        dto.setAvgScore(dto.getAvgScore().setScale(1, RoundingMode.HALF_UP));
                    }
                } else {
                    dto.setExcellentRate(BigDecimal.ZERO);
                }
            }
        }
        
        return stats;
    }

    @Override
    public List<DormScoreDTO> getAllDormScores() {
        return baseMapper.selectDormScores();
    }

    @Override
    public List<InspectionRecord> getRecordsByDormId(Long dormId) {
        // 1. 查询该宿舍的所有主记录
        LambdaQueryWrapper<InspectionRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InspectionRecord::getDormId, dormId)
                     .orderByDesc(InspectionRecord::getCheckDate);
        List<InspectionRecord> records = this.list(recordWrapper);

        if (records.isEmpty()) {
            return records;
        }

        // 2. 收集所有记录ID
        List<Long> recordIds = records.stream()
                                      .map(InspectionRecord::getId)
                                      .collect(Collectors.toList());

        // 3. 批量查询明细
        LambdaQueryWrapper<InspectionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(InspectionDetail::getRecordId, recordIds);
        List<InspectionDetail> details = inspectionDetailMapper.selectList(detailWrapper);

        // 4. 将明细分组并填充到主记录中 (需要在 Entity 中添加非数据库字段 details)
        Map<Long, List<InspectionDetail>> detailsMap = details.stream()
                .collect(Collectors.groupingBy(InspectionDetail::getRecordId));

        records.forEach(record -> {
            record.setDetails(detailsMap.get(record.getId()));
        });

        return records;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRecord(InspectionSubmitDTO dto) {
        try {
            System.out.println("Service processing submitRecord: " + dto);
            
            // 1. 获取总分 (直接使用前端传递的总分)
            BigDecimal totalScore = dto.getTotalScore();
            if (totalScore == null) {
                // 如果前端未传总分，作为兜底，尝试累加明细分数（虽然新逻辑可能不再包含明细分数）
                totalScore = BigDecimal.ZERO;
                if (dto.getDetails() != null) {
                    for (InspectionSubmitDTO.DetailDTO detail : dto.getDetails()) {
                        if (detail.getScore() != null) {
                            totalScore = totalScore.add(detail.getScore());
                        }
                    }
                }
            }

            // 2. 保存主记录
            InspectionRecord record = new InspectionRecord();
            record.setDormId(dto.getDormId());
            record.setInspectorName(dto.getInspectorName());
            record.setIsNotice(Boolean.TRUE.equals(dto.getIsNotice()) ? 1 : 0);
            record.setRemark(dto.getRemark());
            record.setImageUrl(dto.getImageUrl());
            record.setCheckDate(LocalDateTime.now());
            record.setTotalScore(totalScore);
            
            // 初始化整改状态
            if (Boolean.TRUE.equals(dto.getIsNeedRectification())) {
                record.setRectificationStatus(4); // 4: 需整改
            } else {
                record.setRectificationStatus(0); // 0: 无
            }
            
            boolean saved = this.save(record);
            if (!saved) {
                return false;
            }

            // 3. 保存明细
            if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
                List<InspectionDetail> details = new ArrayList<>();
                for (InspectionSubmitDTO.DetailDTO detailDto : dto.getDetails()) {
                    // 只有当有扣分原因时才保存明细
                    String reason = detailDto.getDeductionReason();
                    if (reason != null && !reason.trim().isEmpty()) {
                        InspectionDetail detail = new InspectionDetail();
                        detail.setRecordId(record.getId());
                        detail.setItemId(detailDto.getItemId());
                        // 明细分数可以为空，或者存0
                        detail.setScore(detailDto.getScore() != null ? detailDto.getScore() : BigDecimal.ZERO);
                        detail.setDeductionReason(reason);
                        detail.setImageUrl(detailDto.getImageUrl());
                        details.add(detail);
                    }
                }
                
                // 批量插入明细
                if (!details.isEmpty()) {
                    for (InspectionDetail detail : details) {
                        inspectionDetailMapper.insert(detail);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Submit Record Error: " + e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }

    @Override
    public boolean submitRectification(RectificationSubmitDTO dto) {
        InspectionRecord record = this.getById(dto.getRecordId());
        if (record == null) {
            return false;
        }
        
        record.setRectificationDesc(dto.getDescription());
        record.setRectificationImageUrl(dto.getImageUrl());
        record.setRectificationStatus(1); // 1: 待审核
        
        return this.updateById(record);
    }

    @Override
    public boolean reviewRectification(RectificationReviewDTO dto) {
        InspectionRecord record = this.getById(dto.getRecordId());
        if (record == null) {
            return false;
        }
        
        if (Boolean.TRUE.equals(dto.getPass())) {
            record.setRectificationStatus(2); // 2: 通过
            // 可选：清除备注中的不合格说明，或者追加"已整改"
            if (record.getRemark() == null) {
                record.setRemark("已整改");
            } else if (!record.getRemark().contains("已整改")) {
                record.setRemark(record.getRemark() + " (已整改)");
            }
        } else {
            record.setRectificationStatus(3); // 3: 驳回
            // 可以在备注或新字段中记录驳回原因，这里简单起见追加到整改描述或单独字段
            // 暂时假设前端通过 rectificationStatus=3 判断
            if (dto.getRejectReason() != null && !dto.getRejectReason().isEmpty()) {
                // 如果需要存储驳回原因，可以在 Entity 增加 rejectReason 字段
                // 或者追加到 rectificationDesc
                record.setRectificationDesc(record.getRectificationDesc() + " [驳回原因: " + dto.getRejectReason() + "]");
            }
        }
        
        return this.updateById(record);
    }

    @Override
    public List<InspectionRecord> getPendingRectifications() {
        LambdaQueryWrapper<InspectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionRecord::getRectificationStatus, 1) // 1: 待审核
               .orderByAsc(InspectionRecord::getCheckDate); // 按时间正序
        List<InspectionRecord> records = this.list(wrapper);
        
        if (!records.isEmpty()) {
            List<Long> dormIds = records.stream().map(InspectionRecord::getDormId).distinct().collect(Collectors.toList());
            if (!dormIds.isEmpty()) {
                List<Dormitory> dorms = dormitoryMapper.selectBatchIds(dormIds);
                Map<Long, Dormitory> dormMap = dorms.stream().collect(Collectors.toMap(Dormitory::getId, d -> d));
                
                for (InspectionRecord record : records) {
                    Dormitory dorm = dormMap.get(record.getDormId());
                    if (dorm != null) {
                        record.setBuildingName(dorm.getBuildingName());
                        record.setRoomNumber(dorm.getRoomNumber());
                    }
                }
            }
        }
        
        return records;
    }

    @Override
    public List<InspectionRecord> getRecentRecords(Integer limit) {
        LambdaQueryWrapper<InspectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InspectionRecord::getId) // Assuming ID is auto-increment and reflects insertion order
               .last("LIMIT " + (limit != null ? limit : 50));
        
        List<InspectionRecord> records = this.list(wrapper);
        
        if (!records.isEmpty()) {
            List<Long> dormIds = records.stream().map(InspectionRecord::getDormId).distinct().collect(Collectors.toList());
            if (!dormIds.isEmpty()) {
                List<Dormitory> dorms = dormitoryMapper.selectBatchIds(dormIds);
                Map<Long, Dormitory> dormMap = dorms.stream().collect(Collectors.toMap(Dormitory::getId, d -> d));
                
                for (InspectionRecord record : records) {
                    Dormitory dorm = dormMap.get(record.getDormId());
                    if (dorm != null) {
                        record.setBuildingName(dorm.getBuildingName());
                        record.setRoomNumber(dorm.getRoomNumber());
                    }
                }
            }
        }
        
        return records;
    }

    @Override
    public List<Map<String, Object>> getTopIssues() {
        return inspectionDetailMapper.selectTopIssues();
    }
}
