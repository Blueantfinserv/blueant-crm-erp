package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import org.springframework.stereotype.Component;

@Component
public class MeetingOutcomeValidator {

    public void validateOutcome(Meeting meeting, MeetingOutcome outcome) {

        if (meeting == null) {
            throw new IllegalArgumentException("Meeting not found.");
        }

        if (outcome == null) {
            throw new IllegalArgumentException("Meeting outcome is required.");
        }
    }

}