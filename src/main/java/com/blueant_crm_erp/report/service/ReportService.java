package com.blueant_crm_erp.report.service;

public interface ReportService {
    // Generate various reports (Daily, Weekly, Monthly, Sales, Leader)
    Object generateDailyReport();
    Object generateSalespersonReport(Long salespersonId);
}
