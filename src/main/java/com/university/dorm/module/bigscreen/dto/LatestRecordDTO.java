package com.university.dorm.module.bigscreen.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LatestRecordDTO {
    private String dorm; // "北10栋-340"
    private BigDecimal score;
    private String issues;
    private String time;
    private String inspectorName;
}
