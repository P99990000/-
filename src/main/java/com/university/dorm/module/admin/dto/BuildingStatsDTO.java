package com.university.dorm.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuildingStatsDTO {
    private String building;
    private BigDecimal avgScore;
    private Integer totalChecks;
    private BigDecimal excellentRate;
    private Integer excellentCount;
    private Integer failCount;
}
