package com.university.dorm.module.inspection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.*;

@Data
public class InspectionSubmitDTO {
    @NotNull(message = "宿舍ID不能为空")
    private Long dormId;

    @NotBlank(message = "检查员姓名不能为空")
    private String inspectorName;

    @NotNull(message = "总分不能为空")
    @DecimalMin(value = "0.0", message = "分数不能小于0")
    @DecimalMax(value = "100.0", message = "分数不能超过100")
    private BigDecimal totalScore;

    private Boolean isNotice;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    private String imageUrl;
    private Boolean isNeedRectification;
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long itemId;
        private BigDecimal score;
        private String deductionReason;
        private String imageUrl;
    }
}
