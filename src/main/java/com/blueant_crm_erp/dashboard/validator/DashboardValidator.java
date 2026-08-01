package com.blueant_crm_erp.dashboard.validator;

import com.blueant_crm_erp.dashboard.exception.InvalidDashboardRequestException;
import org.springframework.stereotype.Component;

@Component
public class DashboardValidator {

    public void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidDashboardRequestException("User ID must be valid");
        }
    }
}
