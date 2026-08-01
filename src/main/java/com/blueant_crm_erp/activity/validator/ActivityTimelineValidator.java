package com.blueant_crm_erp.activity.validator;

import com.blueant_crm_erp.activity.dto.request.CreateActivityTimelineRequest;
import com.blueant_crm_erp.activity.exception.InvalidActivityException;
import org.springframework.stereotype.Component;

@Component
public class ActivityTimelineValidator {

    public void validateCreateRequest(CreateActivityTimelineRequest request) {
        if (request.getLeadId() == null) {
            throw new InvalidActivityException("Lead ID is required to log an activity");
        }
        if (request.getActivityType() == null) {
            throw new InvalidActivityException("Activity Type is required");
        }
    }
}
