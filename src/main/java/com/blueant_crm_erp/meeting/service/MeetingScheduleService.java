package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.RescheduleMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;

import java.time.LocalDate;
import java.util.List;

public interface MeetingScheduleService {

    MeetingResponse scheduleMeeting(CreateMeetingRequest request, String currentUserEmail);

    MeetingResponse rescheduleMeeting(String meetingCode, RescheduleMeetingRequest request, String currentUserEmail);

    void cancelMeeting(String meetingCode, String currentUserEmail);

    List<MeetingDetailResponse> getTodayMeetings();

    List<MeetingDetailResponse> getUpcomingMeetings();

    List<MeetingDetailResponse> getMeetingsByDate(LocalDate meetingDate);

}