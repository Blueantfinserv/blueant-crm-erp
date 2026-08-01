package com.blueant_crm_erp.dashboard.mapper;

import com.blueant_crm_erp.dashboard.dto.DashboardSummaryResponse;
import com.blueant_crm_erp.dashboard.dto.PipelineResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DashboardMapper {

    public DashboardSummaryResponse toSummaryResponse(Map<String, Object> data) {
        return DashboardSummaryResponse.builder()
                .totalLeads(((Number) data.getOrDefault("totalLeads", 0L)).longValue())
                .todaysLeads(((Number) data.getOrDefault("todaysLeads", 0L)).longValue())
                .todaysMeetings(((Number) data.getOrDefault("todaysMeetings", 0L)).longValue())
                .pendingFollowups(((Number) data.getOrDefault("pendingFollowups", 0L)).longValue())
                .convertedLeads(((Number) data.getOrDefault("convertedLeads", 0L)).longValue())
                .activePipeline(((Number) data.getOrDefault("activePipeline", 0L)).longValue())
                .revenue(new BigDecimal(data.getOrDefault("revenue", "0").toString()))
                .monthlyTarget(new BigDecimal(data.getOrDefault("monthlyTarget", "0").toString()))
                .targetAchievementPercentage(((Number) data.getOrDefault("targetAchievementPercentage", 0.0)).doubleValue())
                .build();
    }
}
