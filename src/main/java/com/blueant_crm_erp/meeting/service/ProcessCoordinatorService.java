package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;

public interface ProcessCoordinatorService {
    MeetingResponse verifyMeeting(String meetingCode, com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest request, String currentUserEmail);
    MeetingResponse rejectMeeting(String meetingCode, String reason, String currentUserEmail);
}
