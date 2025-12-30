package com.university.dorm.module.inspection.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.inspection.dto.InspectionSubmitDTO;
import com.university.dorm.module.inspection.dto.InspectorSubmitRequest;
import com.university.dorm.module.inspection.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inspector")
public class InspectorController {

    @Autowired
    private InspectionService inspectionService;

    @PostMapping("/submit-record")
    public Result<Boolean> submitRecord(@RequestBody InspectorSubmitRequest request) {
        // Map request to service DTO
        InspectionSubmitDTO serviceDto = new InspectionSubmitDTO();
        serviceDto.setDormId(request.getDormitoryId());
        // Use inspector name from request, default to "系统录入" if empty
        serviceDto.setInspectorName(request.getInspectorName() != null && !request.getInspectorName().isEmpty() ? request.getInspectorName() : "系统录入");
        serviceDto.setTotalScore(request.getTotalScore());
        serviceDto.setIsNotice(request.getIsNotice());
        serviceDto.setRemark("日常检查");  
        
        if (request.getDetails() != null) {
            serviceDto.setDetails(request.getDetails().stream().map(d -> {
                InspectionSubmitDTO.DetailDTO detailDTO = new InspectionSubmitDTO.DetailDTO();
                detailDTO.setItemId(d.getItemId());
                detailDTO.setScore(d.getScore());
                detailDTO.setDeductionReason(d.getRemark());
                return detailDTO;
            }).collect(Collectors.toList()));
        }

        boolean success = inspectionService.submitRecord(serviceDto);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("提交失败");
        }
    }
}
