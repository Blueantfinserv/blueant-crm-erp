package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.exception.lead.LeadTerminalStateException;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * ============================================================================
 * Meeting Workflow Validator (Redesigned)
 * ============================================================================
 */
@Component
public class MeetingWorkflowValidator {

    /** Lead statuses that block any new meeting creation */
    private static final Set<LeadStatus> BLOCKING_LEAD_STATUSES = Set.of(
            LeadStatus.CONVERTED,
            LeadStatus.ALREADY_CLIENT,
            LeadStatus.REMOVED,
            LeadStatus.LOST,
            LeadStatus.NOT_INTERESTED
    );

    private final SalesRoleResolver salesRoleResolver;

    public MeetingWorkflowValidator() {
        this.salesRoleResolver = new SalesRoleResolver();
    }

    public MeetingWorkflowValidator(SalesRoleResolver salesRoleResolver) {
        this.salesRoleResolver = salesRoleResolver != null ? salesRoleResolver : new SalesRoleResolver();
    }

    /**
     * Validates the workflow request payload without meeting context.
     * Preserved for backward compatibility.
     */
    public void validate(MeetingWorkflowRequest request) {
        validate(null, request);
    }

    /**
     * Validates the workflow request payload with role-aware checks based on the meeting's assigned owner.
     */
    public void validate(Meeting meeting, MeetingWorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Workflow request cannot be null.");
        }

        if (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED) {
            validateNotConducted(meeting, request);
            return;
        }

        if (request.getMeetingConducted() != null && request.getMeetingConducted() != MeetingConductStatus.CONDUCTED) {
            throw new IllegalArgumentException("Meeting Workflow Update only supports conducted meetings.");
        }

        if (request.getLeadStatus() == null) {
            throw new IllegalArgumentException("Lead Status is mandatory when meeting is conducted.");
        }

        // Validate aloneWith value (Mandatory)
        if (request.getAloneWith() == null || request.getAloneWith().trim().isEmpty()) {
            throw new IllegalArgumentException("Joined with (aloneWith) is mandatory.");
        }

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

        SalesRole role = salesRoleResolver.resolve(meeting);

        // Validate nextPlanDate if provided
        if (request.getNextPlanDate() != null) {
            if (role == SalesRole.RM) {
                if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("RM follow-up date cannot be in the past.");
                }
                if (request.getNextPlanDate().isAfter(LocalDate.now().plusMonths(1))) {
                    throw new IllegalArgumentException("RM follow-up date cannot be more than one month from today.");
                }
            } else if (role == SalesRole.SC) {
                if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("SC follow-up date cannot be in the past.");
                }
                if (request.getNextPlanDate().isAfter(LocalDate.now().plusMonths(1))) {
                    throw new IllegalArgumentException("SC follow-up date cannot be more than one month from today.");
                }
            } else {
                if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
                }
            }
        }

        // Validate GPS coordinates and accuracy
        if (role == SalesRole.RM) {
            // RM requires latitude, longitude, and accuracy for CONDUCTED meetings
            if (request.getLatitude() == null) {
                throw new IllegalArgumentException("Latitude is required for RM conducted meeting.");
            }
            if (request.getLongitude() == null) {
                throw new IllegalArgumentException("Longitude is required for RM conducted meeting.");
            }
            if (request.getAccuracy() == null) {
                throw new IllegalArgumentException("Location accuracy is required for RM conducted meeting.");
            }
            if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 ||
                request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                throw new IllegalArgumentException("Latitude must be between -90 and +90 degrees.");
            }
            if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 ||
                request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new IllegalArgumentException("Longitude must be between -180 and +180 degrees.");
            }
            if (request.getAccuracy() < 0) {
                throw new IllegalArgumentException("Location accuracy must not be negative.");
            }
        } else {
            // Existing SM / default behavior: Geo coordinates and accuracy are optional
            if (request.getLatitude() != null || request.getLongitude() != null) {
                if (request.getLatitude() == null || request.getLongitude() == null) {
                    throw new IllegalArgumentException("Both latitude and longitude must be provided together.");
                }
                if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 ||
                    request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                    throw new IllegalArgumentException("Latitude must be between -90 and +90 degrees.");
                }
                if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 ||
                    request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                    throw new IllegalArgumentException("Longitude must be between -180 and +180 degrees.");
                }
            }

            if (request.getAccuracy() != null && request.getAccuracy() < 0) {
                throw new IllegalArgumentException("Location accuracy must not be negative.");
            }
        }
    }

    private void validateNotConducted(Meeting meeting, MeetingWorkflowRequest request) {
        // Remarks / reason is mandatory
        String remarks = (request.getRemarks() != null && !request.getRemarks().isBlank())
                ? request.getRemarks()
                : request.getReason();
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new IllegalArgumentException("Remarks/reason is mandatory when meeting is not conducted.");
        }

        // Next plan date is mandatory
        if (request.getNextPlanDate() == null) {
            throw new IllegalArgumentException("Next plan date is mandatory when meeting is not conducted.");
        }

        SalesRole role = salesRoleResolver.resolve(meeting);
        if (role == SalesRole.RM) {
            if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("RM follow-up date cannot be in the past.");
            }
            if (request.getNextPlanDate().isAfter(LocalDate.now().plusMonths(1))) {
                throw new IllegalArgumentException("RM follow-up date cannot be more than one month from today.");
            }
        } else if (role == SalesRole.SC) {
            if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("SC follow-up date cannot be in the past.");
            }
            if (request.getNextPlanDate().isAfter(LocalDate.now().plusMonths(1))) {
                throw new IllegalArgumentException("SC follow-up date cannot be more than one month from today.");
            }
        } else {
            if (request.getNextPlanDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
            }
        }

        if (role == SalesRole.SC) {
            // For SC, geo is optional even for not conducted meetings
            if (request.getLatitude() != null || request.getLongitude() != null) {
                if (request.getLatitude() == null || request.getLongitude() == null) {
                    throw new IllegalArgumentException("Both latitude and longitude must be provided together.");
                }
                if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 ||
                    request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                    throw new IllegalArgumentException("Latitude must be between -90 and +90 degrees.");
                }
                if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 ||
                    request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                    throw new IllegalArgumentException("Longitude must be between -180 and +180 degrees.");
                }
            }
            if (request.getAccuracy() != null && request.getAccuracy() < 0) {
                throw new IllegalArgumentException("Location accuracy must not be negative.");
            }
        } else {
            // Location coordinates and accuracy are mandatory for attempted visit verification
            if (request.getLatitude() == null || request.getLongitude() == null) {
                throw new IllegalArgumentException("Location coordinates (latitude and longitude) are mandatory when meeting is not conducted.");
            }
            if (request.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0 ||
                request.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0) {
                throw new IllegalArgumentException("Latitude must be between -90 and +90 degrees.");
            }
            if (request.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0 ||
                request.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new IllegalArgumentException("Longitude must be between -180 and +180 degrees.");
            }
            if (request.getAccuracy() == null) {
                throw new IllegalArgumentException("Location accuracy is mandatory when meeting is not conducted.");
            }
            if (request.getAccuracy() < 0) {
                throw new IllegalArgumentException("Location accuracy must not be negative.");
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

    /**
     * Validates the transition itself between the current meeting state and request parameters.
     */
    public void validateWorkflowTransition(Meeting meeting, MeetingWorkflowRequest request) {
        if (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED) {
            return;
        }
        if (request.getNextPlanDate() != null && request.getLeadStatus() == MeetingLeadStatus.WORK_IN_PROGRESS) {
            if (meeting.getMeetingNumber() >= 10) {
                throw new IllegalArgumentException("Maximum allowed meeting sequence reached. Cannot schedule Meeting #10.");
            }
        }
    }
}
