package com.university.dorm.module.report.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.report.dto.ReportDetailDTO;
import com.university.dorm.module.report.dto.ReportRankDTO;
import com.university.dorm.module.report.dto.ReportSummaryDTO;
import com.university.dorm.module.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public Result<ReportSummaryDTO> getSummary(@RequestParam(defaultValue = "week") String timeRange) {
        return Result.success(reportService.getSummary(timeRange));
    }

    @GetMapping("/rank/top")
    public Result<List<ReportRankDTO>> getTopRank(@RequestParam(defaultValue = "week") String timeRange) {
        return Result.success(reportService.getTopRank(timeRange));
    }

    @GetMapping("/rank/bottom")
    public Result<List<ReportRankDTO>> getBottomRank(@RequestParam(defaultValue = "week") String timeRange) {
        return Result.success(reportService.getBottomRank(timeRange));
    }

    @GetMapping("/details")
    public Result<List<ReportDetailDTO>> getDetails(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(required = false) String search) {
        return Result.success(reportService.getDetails(timeRange, search));
    }
}
