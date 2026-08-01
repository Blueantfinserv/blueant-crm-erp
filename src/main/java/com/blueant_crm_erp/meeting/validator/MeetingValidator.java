package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MeetingValidator {

    public void validateCreate(CreateMeetingRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Meeting request cannot be null.");
        }

        if (request.getLeadId() == null) {
            throw new IllegalArgumentException("Lead ID is required.");
        }

        if (request.getMeetingMode() == null) {
            throw new IllegalArgumentException("Meeting mode is required.");
        }

        if (request.getMeetingDate() == null) {
            throw new IllegalArgumentException("Meeting date is required.");
        }

        if (request.getMeetingTime() == null) {
            throw new IllegalArgumentException("Meeting time is required.");
        }

        if (!StringUtils.hasText(request.getMeetingLocation())) {
            throw new IllegalArgumentException("Meeting location is required.");
        }
    }

    public void validateUpdate(Long meetingId,
                               UpdateMeetingRequest request,
                               Meeting meeting) {

        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting id is required.");
        }

        if (meeting == null) {
            throw new IllegalArgumentException("Meeting not found.");
        }

        if (request == null) {
            throw new IllegalArgumentException("Update request cannot be null.");
        }

        if (com.blueant_crm_erp.meeting.enums.MeetingStatus.COMPLETED.equals(meeting.getMeetingStatus()) ||
            com.blueant_crm_erp.meeting.enums.MeetingStatus.CANCELLED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException("Cannot modify a completed or cancelled meeting.");
        }
    }

    public void validateMeeting(Meeting meeting) {

        if (meeting == null) {
            throw new IllegalArgumentException("Meeting not found.");
        }
    }

    /**
     * Validates the sales-friendly workflow request.
     *
     * Business rules enforced:
     * - When scheduleNextMeeting = true, both nextMeetingDate and nextMeetingTime are mandatory.
     * - nextMeetingDate cannot be in the past.
     */
    public void validateWorkflow(MeetingWorkflowRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Workflow request cannot be null.");
        }

        boolean isTerminal = request.getMeetingOutcome() == com.blueant_crm_erp.meeting.enums.MeetingOutcome.CONVERTED ||
                             request.getMeetingOutcome() == com.blueant_crm_erp.meeting.enums.MeetingOutcome.REJECTED ||
                             request.getMeetingOutcome() == com.blueant_crm_erp.meeting.enums.MeetingOutcome.DOCUMENT_PENDING;

        if (!isTerminal) {
            if (request.getNextMeetingDate() == null || request.getNextMeetingTime() == null) {
                throw new IllegalArgumentException("Next meeting date and time are required for continuous follow-up (unless outcome is Converted, Rejected, or Document Pending).");
            }

            if (request.getNextMeetingDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST);
            }
        }
    }
}