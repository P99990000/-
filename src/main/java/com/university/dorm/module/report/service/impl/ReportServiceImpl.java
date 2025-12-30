package com.university.dorm.module.report.service.impl;

import com.university.dorm.module.report.dto.ReportDetailDTO;
import com.university.dorm.module.report.dto.ReportRankDTO;
import com.university.dorm.module.report.dto.ReportSummaryDTO;
import com.university.dorm.module.report.mapper.ReportMapper;
import com.university.dorm.module.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public ReportSummaryDTO getSummary(String timeRange) {
        LocalDate startDate = getStartDate(timeRange);
        
        Map<String, Object> stats = reportMapper.getStats(startDate);
        Integer totalDormsInDb = reportMapper.getTotalDormCount();
        
        ReportSummaryDTO summary = new ReportSummaryDTO();
        
        long checkedDorms = stats.get("checkedDorms") == null ? 0 : ((Number) stats.get("checkedDorms")).longValue();
        double avgScore = stats.get("avgScore") == null ? 0.0 : ((Number) stats.get("avgScore")).doubleValue();
        long passCount = stats.get("passCount") == null ? 0 : ((Number) stats.get("passCount")).longValue();
        long failCount = stats.get("failCount") == null ? 0 : ((Number) stats.get("failCount")).longValue();
        long totalChecks = stats.get("totalChecks") == null ? 0 : ((Number) stats.get("totalChecks")).longValue();

        summary.setTotalDorms((int) checkedDorms);
        summary.setAvgScore(BigDecimal.valueOf(avgScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        summary.setFailCount((int) failCount);
        
        // Coverage
        if (totalDormsInDb > 0) {
            double coverage = (double) checkedDorms / totalDormsInDb * 100;
            summary.setCoverage(BigDecimal.valueOf(coverage).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            summary.setCoverage(0.0);
        }
        
        // Pass Rate
        if (totalChecks > 0) {
            double passRate = (double) passCount / totalChecks * 100;
            summary.setPassRate(BigDecimal.valueOf(passRate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            summary.setPassRate(0.0);
        }
        
        // Trend
        LocalDate prevEndDate = startDate;
        LocalDate prevStartDate = "week".equals(timeRange) ? prevEndDate.minusWeeks(1) : prevEndDate.minusMonths(1);
        
        Double prevAvg = reportMapper.getAvgScoreInRange(prevStartDate, prevEndDate);
        if (prevAvg != null && prevAvg > 0) {
            double trend = ((avgScore - prevAvg) / prevAvg) * 100;
            summary.setAvgScoreTrend(BigDecimal.valueOf(trend).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            summary.setAvgScoreTrend(0.0);
        }
        
        return summary;
    }

    @Override
    public List<ReportRankDTO> getTopRank(String timeRange) {
        LocalDate startDate = getStartDate(timeRange);
        return reportMapper.getRank(startDate, "DESC");
    }

    @Override
    public List<ReportRankDTO> getBottomRank(String timeRange) {
        LocalDate startDate = getStartDate(timeRange);
        return reportMapper.getRank(startDate, "ASC");
    }

    public List<ReportDetailDTO> getDetails(String timeRange, String search) {
        return getDetails(timeRange, search, null);
    }

    @Override
    public List<ReportDetailDTO> getDetails(String timeRange, String search, String filterType) {
        LocalDate startDate = getStartDate(timeRange);
        return reportMapper.getDetails(startDate, search, filterType);
    }

    private LocalDate getStartDate(String timeRange) {
        if ("month".equals(timeRange)) {
            return LocalDate.now().minusMonths(1);
        }
        // Default to week
        return LocalDate.now().minusWeeks(1);
    }
}
