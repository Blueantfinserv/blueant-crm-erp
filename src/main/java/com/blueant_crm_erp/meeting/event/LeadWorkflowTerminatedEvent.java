package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import lombok.Getter;

/**
 * Published when the meeting workflow is terminated due to a terminal status.
 */
@Getter
public class LeadWorkflowTerminatedEvent extends MeetingWorkflowEvent {

    private final MeetingLeadStatus terminalStatus;

    public LeadWorkflowTerminatedEvent(Object source, Meeting meeting, MeetingLeadStatus terminalStatus,
                                       String previousStatus, String triggeredBy) {
        super(source, meeting, "WORKFLOW_TERMINATED", previousStatus,
                "Workflow terminated. Status: " + terminalStatus.getDisplayName(),
                triggeredBy);
        this.terminalStatus = terminalStatus;
    }
}
