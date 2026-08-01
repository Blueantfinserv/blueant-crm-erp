package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import lombok.Getter;

/**
 * Published when a MeetingUpdate record is persisted.
 * Carries the immutable update record for downstream consumers.
 */
@Getter
public class MeetingUpdatedEvent extends MeetingWorkflowEvent {

    private final MeetingUpdate meetingUpdate;

    public MeetingUpdatedEvent(Object source, Meeting meeting, MeetingUpdate meetingUpdate, String triggeredBy) {
        super(source, meeting, "WORKFLOW_UPDATE", null,
                "Meeting update #" + meetingUpdate.getUpdateNumber() + " submitted. Outcome: " + meetingUpdate.getMeetingOutcome(),
                triggeredBy);
        this.meetingUpdate = meetingUpdate;
    }
}
