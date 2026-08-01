package com.blueant_crm_erp.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDataResponse {
    
    private String category;
    private Map<String, Object> metrics;
    private Long generatedTimestamp;
}
