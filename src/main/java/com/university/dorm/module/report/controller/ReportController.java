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

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

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
        return Result.success(reportService.getDetails(timeRange, search, null));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(defaultValue = "week") String timeRange,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String filterType) throws IOException {
        List<ReportDetailDTO> list = reportService.getDetails(timeRange, search, filterType);

        ExcelWriter writer = ExcelUtil.getWriter();
        writer.addHeaderAlias("dormName", "宿舍号");
        writer.addHeaderAlias("totalScore", "总分");
        writer.addHeaderAlias("inspectorName", "检查人");
        writer.addHeaderAlias("checkDate", "检查时间");
        writer.addHeaderAlias("issues", "问题描述");
        writer.setOnlyAlias(true);

        // Set column width to avoid "#######" in date column
        writer.setColumnWidth(0, 20); // dormName
        writer.setColumnWidth(1, 10); // totalScore
        writer.setColumnWidth(2, 15); // inspectorName
        writer.setColumnWidth(3, 25); // checkDate (Important fix)
        writer.setColumnWidth(4, 50); // issues

        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("卫生检查报告", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
