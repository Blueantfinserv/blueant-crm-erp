package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * ============================================================================
 * Meeting Workflow Validator
 * ============================================================================
 *
 * Dedicated validator for the meeting workflow update request.
 * Enforces all business guard rules before the workflow is executed.
 *
 * Business Rules:
 * - FOLLOW_UP_REQUIRED and INTERESTED outcomes require next meeting date + time.
 * - Completed/Cancelled meetings cannot be updated.
 * - Converted, Already Client, or Removed leads cannot receive new meetings.
 * - Next meeting date cannot be in the past.
 * - Outcome is required.
 */
@Component
@RequiredArgsConstructor
public class MeetingWorkflowValidator {

    /** Outcomes that do NOT require a next meeting date */
    private static final Set<MeetingOutcome> TERMINAL_OUTCOMES = Set.of(
            MeetingOutcome.CONVERTED,
            MeetingOutcome.SUCCESS,
            MeetingOutcome.NOT_INTERESTED,
            MeetingOutcome.ALREADY_CLIENT,
            MeetingOutcome.REMOVED,
            MeetingOutcome.REJECTED,
            MeetingOutcome.DOCUMENT_PENDING,
            MeetingOutcome.NO_RESPONSE,
            MeetingOutcome.PENDING
    );

    /** Lead statuses that block any new meeting creation */
    private static final Set<LeadStatus> BLOCKING_LEAD_STATUSES = Set.of(
            LeadStatus.CONVERTED,
            LeadStatus.ALREADY_CLIENT,
            LeadStatus.REMOVED,
            LeadStatus.LOST,
            LeadStatus.NOT_INTERESTED
    );

    /**
     * Validates the workflow request payload.
     */
    public void validate(MeetingWorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Workflow request cannot be null.");
        }
        if (request.getMeetingOutcome() == null) {
            throw new IllegalArgumentException("Meeting outcome is required.");
        }

        boolean requiresFollowUp = !TERMINAL_OUTCOMES.contains(request.getMeetingOutcome());
        if (requiresFollowUp) {
            if (request.getNextMeetingDate() == null || request.getNextMeetingTime() == null) {
                throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_REQUIRED);
            }
            if (request.getNextMeetingDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
            }
        }
    }

    /**
     * Validates the current state of the meeting entity.
     * Prevents updates to meetings already completed or cancelled.
     * Prevents workflow progression for leads in terminal states.
     */
    public void validateMeetingState(Meeting meeting) {
        if (MeetingStatus.COMPLETED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException(MeetingConstants.MEETING_ALREADY_COMPLETED);
        }
        if (MeetingStatus.CANCELLED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException("Cancelled meetings cannot be updated via workflow.");
        }
        if (!MeetingStatus.SCHEDULED.equals(meeting.getMeetingStatus()) &&
                !MeetingStatus.RESCHEDULED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException(MeetingConstants.INVALID_MEETING_STATUS);
        }

        // Block if lead is already in a terminal state
        if (meeting.getLead() != null && meeting.getLead().getLeadStatus() != null) {
            LeadStatus currentLeadStatus = meeting.getLead().getLeadStatus();
            if (BLOCKING_LEAD_STATUSES.contains(currentLeadStatus)) {
                throw new IllegalArgumentException(
                        "Lead is already in a terminal state [" + currentLeadStatus + "]. No further meetings can be processed.");
            }
        }
    }
}
