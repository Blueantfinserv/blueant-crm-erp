package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.WorkflowDecision;
import com.blueant_crm_erp.meeting.service.MeetingDecisionEngine;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * ============================================================================
 * Meeting Decision Engine Implementation
 * ============================================================================
 *
 * Maps each MeetingOutcome to a WorkflowDecision using an EnumMap.
 * This is a pure function — no database access, no event publishing.
 *
 * Adding a new outcome requires only adding an entry to the map.
 */
@Component
public class MeetingDecisionEngineImpl implements MeetingDecisionEngine {

    private static final Map<MeetingOutcome, WorkflowDecision> DECISION_MAP;

    static {
        DECISION_MAP = new EnumMap<>(MeetingOutcome.class);

        // ── Continue workflow ─────────────────────────────────────
        DECISION_MAP.put(MeetingOutcome.FOLLOW_UP_REQUIRED, WorkflowDecision.SCHEDULE_FOLLOW_UP);

        // ── Convert lead ──────────────────────────────────────────
        DECISION_MAP.put(MeetingOutcome.CONVERTED, WorkflowDecision.CONVERT_LEAD);
        DECISION_MAP.put(MeetingOutcome.SUCCESS, WorkflowDecision.CONVERT_LEAD);

        // ── Terminal states ───────────────────────────────────────
        DECISION_MAP.put(MeetingOutcome.NOT_INTERESTED, WorkflowDecision.TERMINATE_WORKFLOW);
        DECISION_MAP.put(MeetingOutcome.ALREADY_CLIENT, WorkflowDecision.TERMINATE_WORKFLOW);
        DECISION_MAP.put(MeetingOutcome.REMOVED, WorkflowDecision.TERMINATE_WORKFLOW);
        DECISION_MAP.put(MeetingOutcome.REJECTED, WorkflowDecision.TERMINATE_WORKFLOW);

        // ── Hold states ───────────────────────────────────────────
        DECISION_MAP.put(MeetingOutcome.NO_RESPONSE, WorkflowDecision.HOLD_LEAD);
        DECISION_MAP.put(MeetingOutcome.PENDING, WorkflowDecision.HOLD_LEAD);

        // ── Document pending / Proposal ───────────────────────────
        DECISION_MAP.put(MeetingOutcome.DOCUMENT_PENDING, WorkflowDecision.DOCUMENT_PENDING);
        DECISION_MAP.put(MeetingOutcome.INTERESTED, WorkflowDecision.DOCUMENT_PENDING);
    }

    @Override
    public WorkflowDecision evaluate(MeetingOutcome outcome) {
        if (outcome == null) {
            throw new IllegalArgumentException("Meeting outcome is required for workflow evaluation.");
        }

        WorkflowDecision decision = DECISION_MAP.get(outcome);
        if (decision == null) {
            throw new IllegalStateException("Unmapped meeting outcome: " + outcome +
                    ". Please update MeetingDecisionEngineImpl.");
        }

        return decision;
    }
}
