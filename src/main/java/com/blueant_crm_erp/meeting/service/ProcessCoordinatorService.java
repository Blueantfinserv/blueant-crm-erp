package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.entity.Meeting;

public interface ProcessCoordinatorService {
    Meeting verifyMeeting(String meetingCode, com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest request, String currentUserEmail);
    Meeting rejectMeeting(String meetingCode, String reason, String currentUserEmail);
}
