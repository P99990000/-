package com.university.dorm.module.inspection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InspectorSubmitRequest {
    private Long dormitoryId;
    private String inspectorName;
    private BigDecimal totalScore;
    private Boolean isNotice;
    private List<Detail> details;

    @Data
    public static class Detail {
        private Long itemId;
        private BigDecimal score;
        private String remark;
    }
}
