package com.university.dorm.module.inspection.dto;

import lombok.Data;

@Data
public class RectificationReviewDTO {
    private Long recordId;
    private Boolean pass; // true for pass, false for reject
    private String rejectReason; // optional
}
