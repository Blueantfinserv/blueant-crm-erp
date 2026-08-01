package com.blueant_crm_erp.analytics.mapper;

import com.blueant_crm_erp.analytics.dto.response.AnalyticsDataResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnalyticsMapper {

    public AnalyticsDataResponse toResponse(String category, Map<String, Object> data) {
        return AnalyticsDataResponse.builder()
                .category(category)
                .metrics(data)
                .generatedTimestamp(System.currentTimeMillis())
                .build();
    }
}
