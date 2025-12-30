package com.university.dorm.module.inspection.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.inspection.entity.InspectionItem;
import com.university.dorm.module.inspection.service.InspectionItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspection-items")
public class InspectionItemController {

    @Autowired
    private InspectionItemService inspectionItemService;

    @GetMapping
    public Result<List<InspectionItem>> getActiveItems() {
        return Result.success(inspectionItemService.getActiveItems());
    }
}
