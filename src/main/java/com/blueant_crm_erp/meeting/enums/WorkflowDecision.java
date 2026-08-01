package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ============================================================================
 * Workflow Decision
 * ============================================================================
 *
 * Returned by the MeetingDecisionEngine after evaluating a MeetingOutcome.
 * The orchestrator (MeetingWorkflowService) uses this to determine the next
 * step in the sales pipeline without embedding business rules itself.
 */
@Getter
@RequiredArgsConstructor
public enum WorkflowDecision {

    /**
     * Client wants another meeting. Auto-create next sequential meeting.
     */
    SCHEDULE_FOLLOW_UP("Schedule Follow-up"),

    /**
     * Client converted. Transition lead to CONVERTED status.
     */
    CONVERT_LEAD("Convert Lead"),

    /**
     * Terminal state. No further meetings should be scheduled.
     * Covers: NOT_INTERESTED, ALREADY_CLIENT, REMOVED, REJECTED.
     */
    TERMINATE_WORKFLOW("Terminate Workflow"),

    /**
     * Lead put on hold. No immediate follow-up but workflow is not terminated.
     */
    HOLD_LEAD("Hold Lead"),

    /**
     * Documents are pending from the client. Workflow pauses.
     */
    DOCUMENT_PENDING("Document Pending");

    private final String displayName;
}
