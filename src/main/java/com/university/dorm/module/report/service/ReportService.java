package com.university.dorm.module.report.service;

import com.university.dorm.module.report.dto.ReportDetailDTO;
import com.university.dorm.module.report.dto.ReportRankDTO;
import com.university.dorm.module.report.dto.ReportSummaryDTO;

import java.util.List;

public interface ReportService {
    ReportSummaryDTO getSummary(String timeRange);
    List<ReportRankDTO> getTopRank(String timeRange);
    List<ReportRankDTO> getBottomRank(String timeRange);
    List<ReportDetailDTO> getDetails(String timeRange, String search);
}
