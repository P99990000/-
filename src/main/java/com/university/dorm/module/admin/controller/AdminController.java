package com.university.dorm.module.admin.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.admin.dto.BuildingStatsDTO;
import com.university.dorm.module.admin.dto.DormScoreDTO;
import com.university.dorm.module.inspection.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private InspectionService inspectionService;

    @GetMapping("/building-stats")
    public Result<List<BuildingStatsDTO>> getBuildingStats() {
        return Result.success(inspectionService.getBuildingStats());
    }

    @GetMapping("/all-dorm-scores")
    public Result<List<DormScoreDTO>> getAllDormScores() {
        return Result.success(inspectionService.getAllDormScores());
    }
}
