package com.university.dorm.module.report.service;

import com.university.dorm.module.report.dto.ReportDetailDTO;
import com.university.dorm.module.report.dto.ReportRankDTO;
import com.university.dorm.module.report.dto.ReportSummaryDTO;

import java.util.List;

public interface ReportService {
    ReportSummaryDTO getSummary(String timeRange);
    List<ReportRankDTO> getTopRank(String timeRange);
    List<ReportRankDTO> getBottomRank(String timeRange);
    /**
     * 获取通报详细列表
     */
    List<ReportDetailDTO> getDetails(String timeRange, String search, String filterType);
}
