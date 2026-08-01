package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;

/**
 * Published when a lead is converted via the meeting workflow.
 * Covers outcomes: CONVERTED, SUCCESS.
 */
public class LeadConvertedEvent extends MeetingWorkflowEvent {

    public LeadConvertedEvent(Object source, Meeting meeting, String previousStatus, String triggeredBy) {
        super(source, meeting, "CONVERTED", previousStatus,
                "Lead converted from meeting: " + meeting.getMeetingCode(),
                triggeredBy);
    }
}
