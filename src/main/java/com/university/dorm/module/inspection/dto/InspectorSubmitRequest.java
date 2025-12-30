package com.university.dorm.module.inspection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InspectorSubmitRequest {
    private Long dormId;
    private String inspectorName;
    private BigDecimal totalScore;
    private Boolean isNotice;
    private Boolean isNeedRectification;
    private String imageUrl;
    private List<Detail> details;

    @Data
    public static class Detail {
        private Long itemId;
        private BigDecimal score;
        private String remark;
    }
}
