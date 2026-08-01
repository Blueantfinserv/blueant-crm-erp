package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.WorkflowDecision;

/**
 * ============================================================================
 * Meeting Decision Engine
 * ============================================================================
 *
 * Pure business logic component implementing the Strategy Pattern.
 *
 * Evaluates a MeetingOutcome and returns a WorkflowDecision.
 * Contains zero side effects — no database access, no event publishing.
 *
 * The orchestrator (MeetingWorkflowService) uses the returned decision
 * to determine the next step in the sales pipeline.
 */
public interface MeetingDecisionEngine {

    /**
     * Evaluates the given meeting outcome and returns the appropriate workflow decision.
     *
     * @param outcome the meeting outcome submitted by sales
     * @return the workflow decision (never null)
     */
    WorkflowDecision evaluate(MeetingOutcome outcome);
}
