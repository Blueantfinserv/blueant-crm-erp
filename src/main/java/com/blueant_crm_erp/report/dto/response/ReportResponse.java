package com.blueant_crm_erp.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private String reportType;
    private Long targetId;
    private Map<String, Object> data;
    private Long generatedTimestamp;
}
