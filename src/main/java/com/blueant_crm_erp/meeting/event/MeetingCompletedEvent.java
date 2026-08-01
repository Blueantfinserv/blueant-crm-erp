package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;

/**
 * Published when a meeting is marked as COMPLETED.
 * Extends MeetingWorkflowEvent for backward compatibility with existing listeners.
 */
public class MeetingCompletedEvent extends MeetingWorkflowEvent {

    public MeetingCompletedEvent(Object source, Meeting meeting, String previousStatus, String description, String triggeredBy) {
        super(source, meeting, "COMPLETED", previousStatus, description, triggeredBy);
    }
}
