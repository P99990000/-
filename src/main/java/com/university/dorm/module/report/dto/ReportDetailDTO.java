package com.university.dorm.module.report.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReportDetailDTO {
    private Long id;
    private String dormName;
    private BigDecimal totalScore;
    private String inspectorName;
    private LocalDateTime checkDate;
    private String issues;
}
