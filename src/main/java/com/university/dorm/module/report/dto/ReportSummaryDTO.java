package com.university.dorm.module.report.dto;

import lombok.Data;

@Data
public class ReportSummaryDTO {
    private Integer totalDorms;
    private Double coverage;
    private Double avgScore;
    private Double avgScoreTrend;
    private Double passRate;
    private Integer failCount;
}
