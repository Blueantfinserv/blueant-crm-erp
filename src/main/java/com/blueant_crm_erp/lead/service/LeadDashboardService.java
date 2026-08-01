package com.blueant_crm_erp.lead.service;

import com.blueant_crm_erp.lead.dto.response.LeadStatisticsResponse;

public interface LeadDashboardService {

    /**
     * Retrieves aggregated dashboard summary.
     * In the future, this will orchestrate calls to Meeting and ServiceRequest modules.
     *
     * @return LeadStatisticsResponse
     */
    LeadStatisticsResponse getDashboardSummary();
}
