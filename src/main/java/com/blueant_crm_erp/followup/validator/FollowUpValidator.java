package com.blueant_crm_erp.followup.validator;

import com.blueant_crm_erp.followup.exception.InvalidFollowUpException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FollowUpValidator {

    public void validateScheduleDate(LocalDate date) {
        if (date != null && date.isBefore(LocalDate.now())) {
            throw new InvalidFollowUpException("Follow-up date cannot be in the past");
        }
    }
}
