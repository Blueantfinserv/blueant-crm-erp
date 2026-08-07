package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
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

        if (request.getMeetingConducted() == null) {
            throw new IllegalArgumentException("Meeting Conducted status is required.");
        }

        // EVERY submission must capture live GPS location.
        // NOTE: capturedAt is NOT required from the frontend — it is generated server-side.
        if (request.getLatitude() == null || request.getLongitude() == null ||
                request.getAccuracy() == null) {
            throw new IllegalArgumentException("GPS parameters (latitude, longitude, accuracy) are mandatory for every submission.");
        }

        if (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED) {
            // CASE 1: Not Conducted
            if (request.getMeetingRemarks() == null || request.getMeetingRemarks().isBlank()) {
                throw new IllegalArgumentException("Remarks are mandatory when meeting is not conducted.");
            }
            if (request.getNextPlanDate() == null) {
                throw new IllegalArgumentException("Next Plan Date is mandatory when meeting is not conducted.");
            }
            if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
            }
        } else {
            // CASE 2: Conducted
            if (request.getLeadStatus() == null) {
                throw new IllegalArgumentException("Lead Status is mandatory when meeting is conducted.");
            }

            // Remarks are mandatory for all Conducted sub-cases
            if (request.getMeetingRemarks() == null || request.getMeetingRemarks().isBlank()) {
                throw new IllegalArgumentException("Remarks are mandatory when meeting is conducted.");
            }

            switch (request.getLeadStatus()) {
                case ALREADY_CLIENT -> {
                    if (request.getCurrentInvestmentCompany() == null || request.getCurrentInvestmentCompany().isBlank()) {
                        throw new IllegalArgumentException("Current Investment Company is mandatory for Already Client status.");
                    }
                    if (request.getCurrentAdvisor() == null || request.getCurrentAdvisor().isBlank()) {
                        throw new IllegalArgumentException("Current Advisor is mandatory for Already Client status.");
                    }
                }
                case CONVERTED_CLIENT -> {
                    if (request.getPanNumber() == null || request.getPanNumber().isBlank()) {
                        throw new IllegalArgumentException("PAN Number is mandatory for Converted Client status.");
                    }
                    if (request.getInvestmentAmount() == null || request.getInvestmentAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Investment Amount must be greater than zero.");
                    }
                    if (request.getInvestmentType() == null) {
                        throw new IllegalArgumentException("Investment Type is mandatory for Converted Client status.");
                    }
                }
                case REMOVE_CLIENT -> {
                    if (request.getReason() == null || request.getReason().isBlank()) {
                        throw new IllegalArgumentException("Reason is mandatory for Remove Client status.");
                    }
                    String r = request.getReason().trim();
                    Set<String> validReasons = Set.of("Duplicate Lead", "Wrong Number", "Fake Lead", "Shifted", "Other");
                    boolean isValid = validReasons.stream().anyMatch(val -> val.equalsIgnoreCase(r));
                    if (!isValid) {
                        throw new IllegalArgumentException("Reason must be one of: Duplicate Lead, Wrong Number, Fake Lead, Shifted, Other.");
                    }
                }
                case CLIENT_NOT_INTERESTED -> {
                    if (request.getReason() == null || request.getReason().isBlank()) {
                        throw new IllegalArgumentException("Reason is mandatory for Client Not Interested status.");
                    }
                    String r = request.getReason().trim();
                    Set<String> validReasons = Set.of("Already Investing", "No Interest", "No Funds", "Need Time", "Other");
                    boolean isValid = validReasons.stream().anyMatch(val -> val.equalsIgnoreCase(r));
                    if (!isValid) {
                        throw new IllegalArgumentException("Reason must be one of: Already Investing, No Interest, No Funds, Need Time, Other.");
                    }
                }
                case WORK_IN_PROGRESS -> {
                    if (request.getNextPlanDate() == null) {
                        throw new IllegalArgumentException("Next Plan Date is mandatory for Work In Progress status.");
                    }
                    if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
                    }
                }
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
                throw new IllegalArgumentException(
                        "Lead is already in a terminal state [" + currentLeadStatus + "]. No further meetings can be processed.");
            }
        }
    }
}
