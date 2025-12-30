package com.university.dorm.module.dormitory.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.dormitory.entity.Dormitory;
import com.university.dorm.module.dormitory.service.DormitoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dormitories")
public class DormitoryController {

    @Autowired
    private DormitoryService dormitoryService;

    @GetMapping
    public Result<List<Dormitory>> list() {
        return Result.success(dormitoryService.list());
    }

    @GetMapping("/buildings")
    public Result<Map<String, List<String>>> getBuildings() {
        return Result.success(dormitoryService.getBuildingsGrouped());
    }

    @GetMapping("/by-building/{buildingName}")
    public Result<List<Dormitory>> getDormsByBuilding(@PathVariable String buildingName) {
        return Result.success(dormitoryService.getDormsByBuilding(buildingName));
    }
}
