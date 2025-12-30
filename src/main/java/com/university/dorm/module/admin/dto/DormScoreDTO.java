package com.university.dorm.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DormScoreDTO {
    private String building;
    private String room;
    private BigDecimal score;
}
