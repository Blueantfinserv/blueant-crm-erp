package com.blueant_crm_erp.analytics.validator;

import com.blueant_crm_erp.analytics.dto.request.AnalyticsQueryRequest;
import com.blueant_crm_erp.analytics.exception.InvalidAnalyticsQueryException;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsValidator {

    public void validateQueryRequest(AnalyticsQueryRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidAnalyticsQueryException("Start date cannot be after end date");
        }
    }
}
