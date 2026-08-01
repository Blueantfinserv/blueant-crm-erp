package com.blueant_crm_erp.analytics.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Object> getMeetingAnalytics(Long userId);
    Map<String, Object> getSalesKpi(Long salespersonId);
}
