package com.university.dorm.module.bigscreen.dto;

import lombok.Data;

@Data
public class ScoreDistributionDTO {
    private String excellent; // e.g. "45%"
    private String good;
    private String pass;
    private String fail;
}
