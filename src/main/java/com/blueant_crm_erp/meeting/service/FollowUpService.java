package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.entity.Meeting;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ============================================================================
 * Follow-Up Service
 * ============================================================================
 *
 * Responsible for creating the next sequential meeting in the sales pipeline.
 *
 * Business Rules:
 * - Meeting numbering must always be sequential (never skip).
 * - Only one active SCHEDULED meeting per Lead at any time.
 * - MeetingType = INTRO for all meetings (business requirement).
 * - Follow-up inherits meeting mode from the current meeting.
 */
public interface FollowUpService {

    /**
     * Creates the next sequential meeting for the lead.
     *
     * @param currentMeeting    the meeting that was just completed
     * @param nextMeetingDate   the scheduled date for the follow-up
     * @param nextMeetingTime   the scheduled time for the follow-up
     * @param remarks           the workflow/outcome remark to copy to the new meeting
     * @param triggeredBy       the authenticated user email
     * @return the newly created follow-up meeting
     */
    Meeting createFollowUp(Meeting currentMeeting, LocalDate nextMeetingDate, LocalTime nextMeetingTime, String remarks, String triggeredBy);
}
