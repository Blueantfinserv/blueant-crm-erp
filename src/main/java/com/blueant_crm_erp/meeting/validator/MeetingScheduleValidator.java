package com.blueant_crm_erp.meeting.validator;

import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class MeetingScheduleValidator {

    /**
     * Validate meeting schedule business rules.
     */
    public void validateSchedule(CreateMeetingRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Meeting request cannot be null.");
        }

        LocalDate meetingDate = request.getMeetingDate();
        LocalTime meetingTime = request.getMeetingTime();

        if (meetingDate == null || meetingTime == null) {
            return;
        }

        LocalDateTime meetingDateTime = LocalDateTime.of(meetingDate, meetingTime);

        if (meetingDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting cannot be scheduled in the past.");
        }

        // Future business validations:
        // - Check employee availability
        // - Check duplicate meeting for same lead
        // - Check overlapping meetings
        // - Check office working hours
        // - Check holiday calendar
    }

}