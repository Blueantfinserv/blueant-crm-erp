package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import lombok.Getter;

/**
 * Published when the meeting workflow is terminated due to a terminal outcome.
 * Covers: NOT_INTERESTED, ALREADY_CLIENT, REMOVED, REJECTED.
 */
@Getter
public class LeadWorkflowTerminatedEvent extends MeetingWorkflowEvent {

    private final MeetingOutcome terminalOutcome;

    public LeadWorkflowTerminatedEvent(Object source, Meeting meeting, MeetingOutcome terminalOutcome,
                                       String previousStatus, String triggeredBy) {
        super(source, meeting, "WORKFLOW_TERMINATED", previousStatus,
                "Workflow terminated. Outcome: " + terminalOutcome.getDisplayName(),
                triggeredBy);
        this.terminalOutcome = terminalOutcome;
    }
}
