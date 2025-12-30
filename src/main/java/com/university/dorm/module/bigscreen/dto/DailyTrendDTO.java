package com.university.dorm.module.bigscreen.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DailyTrendDTO {
    private String date;
    private BigDecimal avgScore;
}
