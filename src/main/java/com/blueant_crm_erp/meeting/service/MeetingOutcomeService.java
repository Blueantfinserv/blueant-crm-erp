package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingOutcomeRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;

import java.util.List;

public interface MeetingOutcomeService {

    MeetingResponse updateMeetingOutcome(String meetingCode, UpdateMeetingOutcomeRequest request, String currentUserEmail);

    MeetingResponse markAsConverted(String meetingCode, String currentUserEmail);

    MeetingResponse markAsRejected(String meetingCode, String currentUserEmail);

    List<MeetingDetailResponse> getMeetingsByOutcome(MeetingOutcome outcome);

}