package com.blueant_crm_erp.report.validator;

import com.blueant_crm_erp.report.exception.InvalidReportException;
import org.springframework.stereotype.Component;

@Component
public class ReportValidator {

    public void validateSalespersonId(Long salespersonId) {
        if (salespersonId == null || salespersonId <= 0) {
            throw new InvalidReportException("Salesperson ID is invalid");
        }
    }
}
