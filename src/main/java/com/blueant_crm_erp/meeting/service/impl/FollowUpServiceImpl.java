package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.FollowUpService;
import com.blueant_crm_erp.util.id.MeetingCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ============================================================================
 * Follow-Up Service Implementation
 * ============================================================================
 *
 * Creates next sequential meeting in the sales pipeline.
 *
 * Business Rules enforced:
 * 1. Meeting number = currentMeeting.meetingNumber + 1 (always sequential).
 * 2. MeetingType = INTRO (business requirement — company treats all meetings as intro).
 * 3. Mode inherited from current meeting.
 * 4. Only one SCHEDULED meeting per lead at a time (validated upstream by MeetingWorkflowService).
 */
@Service("meetingFollowUpServiceImpl")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowUpServiceImpl implements FollowUpService {

    private final MeetingRepository meetingRepository;

    @Override
    public Meeting createFollowUp(Meeting currentMeeting, LocalDate nextMeetingDate, LocalTime nextMeetingTime, String remarks, String triggeredBy) {
        log.info("Creating follow-up meeting after meeting #{} for lead {}",
                currentMeeting.getMeetingNumber(), currentMeeting.getLead().getLeadCode());

        int nextMeetingNumber = currentMeeting.getMeetingNumber() + 1;
        if (nextMeetingNumber > 10) {
            throw new IllegalArgumentException("Maximum allowed meeting sequence reached. Cannot schedule Meeting #10.");
        }

        // Rule 6: Only ONE meeting should have Status = SCHEDULED per lead
        if (meetingRepository.existsByLeadIdAndMeetingStatus(
                currentMeeting.getLead().getId(), MeetingStatus.SCHEDULED)) {
            log.warn("Lead {} already has a SCHEDULED meeting. Skipping next meeting creation.",
                    currentMeeting.getLead().getLeadCode());
            throw new IllegalArgumentException("Lead already has an active scheduled meeting. Cannot create another.");
        }

        Meeting nextMeeting = new Meeting();

        // Generate unique meeting code
        long count = meetingRepository.count() + 1;
        nextMeeting.setMeetingCode(MeetingCodeGenerator.generate(count));

        // Sequential meeting number — never skip
        nextMeeting.setMeetingNumber(nextMeetingNumber);

        // Business rule: First meeting is INTRO, subsequent meetings are FOLLOW_UP
        nextMeeting.setMeetingType(nextMeetingNumber == 1 ? MeetingType.INTRO : MeetingType.FOLLOW_UP);

        // Title
        String title;
        int meetingIndex = nextMeetingNumber - 1; // Since sequence 2 = 1st meeting
        
        if (nextMeetingNumber == 1) {
            title = "Intro Meeting";
        } else if (meetingIndex == 1) {
            title = "1st Meeting";
        } else if (meetingIndex == 2) {
            title = "2nd Meeting";
        } else if (meetingIndex == 3) {
            title = "3rd Meeting";
        } else {
            title = meetingIndex + "th Meeting";
        }
        nextMeeting.setMeetingTitle(title);

        // Inherit from current meeting
        nextMeeting.setLead(currentMeeting.getLead());
        nextMeeting.setAssignedEmployee(currentMeeting.getAssignedEmployee());
        nextMeeting.setMeetingMode(currentMeeting.getMeetingMode());

        // Schedule
        nextMeeting.setMeetingDate(nextMeetingDate);
        nextMeeting.setMeetingTime(nextMeetingTime);
        if ("WORK IN PROGRESS".equalsIgnoreCase(remarks)) {
            nextMeeting.setMeetingRemarks(remarks);
        } else {
            nextMeeting.setMeetingRemarks(null);
        }

        // Status
        nextMeeting.setMeetingStatus(MeetingStatus.SCHEDULED);
        nextMeeting.setStatus(Status.ACTIVE);

        // Also store next meeting date on current meeting for reference
        currentMeeting.setNextMeetingDate(nextMeetingDate);
        currentMeeting.setNextMeetingTime(nextMeetingTime);
        meetingRepository.save(currentMeeting);

        Meeting savedNextMeeting = meetingRepository.save(nextMeeting);
        log.info("Follow-up meeting #{} created: {} for lead {}",
                nextMeetingNumber, savedNextMeeting.getMeetingCode(), currentMeeting.getLead().getLeadCode());

        return savedNextMeeting;
    }
}
