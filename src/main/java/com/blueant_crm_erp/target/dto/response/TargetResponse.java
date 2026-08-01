package com.blueant_crm_erp.target.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetResponse {
    private Long id;
    private Long userId;
    private String targetMonth;
    private BigDecimal revenueTarget;
    private Integer meetingTarget;
    private Integer leadTarget;
    private Integer followupTarget;
}
