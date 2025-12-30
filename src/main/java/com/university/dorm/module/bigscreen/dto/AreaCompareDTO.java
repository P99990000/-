package com.university.dorm.module.bigscreen.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AreaCompareDTO {
    private BigDecimal northAvg;
    private BigDecimal southAvg;
}
