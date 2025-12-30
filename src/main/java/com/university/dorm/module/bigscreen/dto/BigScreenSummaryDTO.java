package com.university.dorm.module.bigscreen.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BigScreenSummaryDTO {
    private Long totalChecks;
    private BigDecimal avgScore;
    private Long todayChecks;
    private String topIssue;
}
