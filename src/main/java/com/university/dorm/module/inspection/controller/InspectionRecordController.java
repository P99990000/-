package com.university.dorm.module.inspection.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.inspection.entity.InspectionRecord;
import com.university.dorm.module.inspection.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import com.university.dorm.module.inspection.dto.InspectionSubmitDTO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.university.dorm.module.inspection.dto.RectificationReviewDTO;
import com.university.dorm.module.inspection.dto.RectificationSubmitDTO;

import cn.hutool.http.HtmlUtil;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/records")
public class InspectionRecordController {

    @Autowired
    private InspectionService inspectionService;

    @GetMapping("/by-dorm/{dormId}")
    public Result<List<InspectionRecord>> getRecordsByDormId(@PathVariable Long dormId) {
        return Result.success(inspectionService.getRecordsByDormId(dormId));
    }

    @PostMapping("/submit")
    public Result<Boolean> submitRecord(@RequestBody @Valid InspectionSubmitDTO dto) {
        // XSS Filtering
        if (dto.getRemark() != null) {
            dto.setRemark(HtmlUtil.escape(dto.getRemark()));
        }
        if (dto.getInspectorName() != null) {
            dto.setInspectorName(HtmlUtil.escape(dto.getInspectorName()));
        }
        if (dto.getDetails() != null) {
            for (InspectionSubmitDTO.DetailDTO detail : dto.getDetails()) {
                if (detail.getDeductionReason() != null) {
                    detail.setDeductionReason(HtmlUtil.escape(detail.getDeductionReason()));
                }
            }
        }
        
        boolean success = inspectionService.submitRecord(dto);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("提交失败");
        }
    }

    @PostMapping("/rectification/submit")
    public Result<Boolean> submitRectification(@RequestBody RectificationSubmitDTO dto) {
        return Result.success(inspectionService.submitRectification(dto));
    }

    @PostMapping("/rectification/review")
    public Result<Boolean> reviewRectification(@RequestBody RectificationReviewDTO dto) {
        return Result.success(inspectionService.reviewRectification(dto));
    }

    @GetMapping("/rectification/pending")
    public Result<List<InspectionRecord>> getPendingRectifications() {
        return Result.success(inspectionService.getPendingRectifications());
    }

    @GetMapping("/recent")
    public Result<List<InspectionRecord>> getRecentRecords(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Result.success(inspectionService.getRecentRecords(limit));
    }
}
