package com.university.dorm.module.report.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReportRankDTO {
    private String dormName;
    private BigDecimal score;
    private String inspector;
}
