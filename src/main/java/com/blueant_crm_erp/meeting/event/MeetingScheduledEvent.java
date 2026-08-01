package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;

/**
 * Published when a meeting is scheduled (initial or follow-up auto-creation).
 */
public class MeetingScheduledEvent extends MeetingWorkflowEvent {

    public MeetingScheduledEvent(Object source, Meeting meeting, String description, String triggeredBy) {
        super(source, meeting, "SCHEDULED", null, description, triggeredBy);
    }
}
