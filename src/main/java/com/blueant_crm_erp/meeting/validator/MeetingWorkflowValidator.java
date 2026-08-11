package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.exception.lead.LeadTerminalStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * ============================================================================
 * Meeting Workflow Validator (Redesigned)
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
public class MeetingWorkflowValidator {

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

        if (request.getMeetingConducted() != null && request.getMeetingConducted() != MeetingConductStatus.CONDUCTED) {
            throw new IllegalArgumentException("Meeting Workflow Update only supports conducted meetings.");
        }

        if (request.getLeadStatus() == null) {
            throw new IllegalArgumentException("Lead Status is mandatory when meeting is conducted.");
        }

        // Validate aloneWith value if provided
        if (request.getAloneWith() != null) {
            String aw = request.getAloneWith().trim();
            if (!"SELF".equalsIgnoreCase(aw) && !"SOMEONE".equalsIgnoreCase(aw)) {
                throw new IllegalArgumentException("Alone with must be either SELF or SOMEONE.");
            }
            if ("SELF".equalsIgnoreCase(aw)) {
                if ((request.getPersonName() != null && !request.getPersonName().isBlank()) ||
                    (request.getPosition() != null && !request.getPosition().isBlank())) {
                    throw new IllegalArgumentException("Person name and position must be null when aloneWith is SELF.");
                }
            }
        }

        // Validate nextPlanDate if provided
        if (request.getNextPlanDate() != null) {
            if (request.getNextPlanDate().isBefore(LocalDate.now())) {
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
        if (MeetingStatus.NOT_CONDUCTED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException("Not conducted meetings cannot be updated via workflow.");
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
                throw new LeadTerminalStateException(
                        "Lead is already in a terminal state [" + currentLeadStatus + "]. No further meetings can be processed.");
            }
        }
    }
}
