package com.university.dorm.module.inspection.dto;

import lombok.Data;

@Data
public class RectificationSubmitDTO {
    private Long recordId;
    private String description;
    private String imageUrl;
}
