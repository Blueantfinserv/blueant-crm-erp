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

        if (request.getMeetingStatus() != com.blueant_crm_erp.meeting.enums.MeetingStatus.COMPLETED) {
            java.time.LocalTime time = request.getMeetingTime() != null ? request.getMeetingTime() : java.time.LocalTime.MIDNIGHT;
            java.time.LocalDateTime meetingDateTime = java.time.LocalDateTime.of(request.getMeetingDate(), time);
            if (meetingDateTime.isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("Meeting date cannot be in the past.");
            }
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

        if (meeting.getMeetingStatus() == com.blueant_crm_erp.meeting.enums.MeetingStatus.SCHEDULED) {
            if (request.getMeetingDate() != null && request.getMeetingStatus() != com.blueant_crm_erp.meeting.enums.MeetingStatus.COMPLETED) {
                java.time.LocalTime time = request.getMeetingTime() != null ? request.getMeetingTime() : (meeting.getMeetingTime() != null ? meeting.getMeetingTime() : java.time.LocalTime.MIDNIGHT);
                java.time.LocalDateTime newDateTime = java.time.LocalDateTime.of(request.getMeetingDate(), time);
                if (newDateTime.isBefore(java.time.LocalDateTime.now())) {
                    throw new IllegalArgumentException("Meeting date cannot be in the past.");
                }
            }
        }
    }

    public void validateMeeting(Meeting meeting) {

        if (meeting == null) {
            throw new IllegalArgumentException("Meeting not found.");
        }
    }

}
