package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;
import lombok.Getter;

/**
 * Published when a follow-up meeting is auto-created by the workflow engine.
 */
@Getter
public class FollowUpCreatedEvent extends MeetingWorkflowEvent {

    private final Meeting followUpMeeting;

    public FollowUpCreatedEvent(Object source, Meeting currentMeeting, Meeting followUpMeeting, String triggeredBy) {
        super(source, followUpMeeting, "FOLLOW_UP_CREATED", null,
                "Auto-scheduled follow-up meeting #" + followUpMeeting.getMeetingNumber(),
                triggeredBy);
        this.followUpMeeting = followUpMeeting;
    }
}
