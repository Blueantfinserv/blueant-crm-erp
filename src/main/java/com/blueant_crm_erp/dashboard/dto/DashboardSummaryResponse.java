package com.blueant_crm_erp.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private long totalLeads;
    private long todaysLeads;
    private long todaysMeetings;
    private long pendingFollowups;
    private long convertedLeads;
    private long activePipeline;
    private BigDecimal revenue;
    private BigDecimal monthlyTarget;
    private double targetAchievementPercentage;
}
