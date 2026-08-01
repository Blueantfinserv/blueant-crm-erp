package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.entity.Meeting;

public interface ProcessCoordinatorService {
    Meeting verifyMeeting(String meetingCode, String remarks, String currentUserEmail);
    Meeting rejectMeeting(String meetingCode, String reason, String currentUserEmail);
}
