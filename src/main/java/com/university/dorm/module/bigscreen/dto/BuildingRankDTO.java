package com.university.dorm.module.bigscreen.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuildingRankDTO {
    private String building;
    private BigDecimal avgScore;
}
