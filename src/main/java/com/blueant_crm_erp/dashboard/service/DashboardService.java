package com.blueant_crm_erp.dashboard.service;

import com.blueant_crm_erp.dashboard.dto.DashboardSummaryResponse;
import com.blueant_crm_erp.dashboard.dto.PipelineResponse;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary(Long userId);
    PipelineResponse getPipelineSummary(Long userId);
}
