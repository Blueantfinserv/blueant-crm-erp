package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingSearchRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.ActiveMeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDropdownResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeetingService {

    MeetingResponse createMeeting(CreateMeetingRequest request, String currentUserEmail);

    com.blueant_crm_erp.meeting.entity.Meeting createInitialMeeting(com.blueant_crm_erp.lead.entity.Lead lead);

    MeetingResponse updateMeeting(String meetingCode, UpdateMeetingRequest request, String currentUserEmail);

    MeetingDetailResponse getMeetingByCode(String meetingCode);

    MeetingDetailResponse getMeetingById(Long id);

    PageResponse<MeetingSummaryResponse> searchMeetings(MeetingSearchRequest request, Pageable pageable);

    List<MeetingResponse> getAllMeetings(String search, String date, String status, Integer sequence,
                                         com.blueant_crm_erp.servicerequest.enums.VerificationStatus verificationStatus,
                                         Long salesPersonId, String salesPersonName);

    List<MeetingDropdownResponse> getMeetingDropdown();

    void deleteMeeting(String meetingCode, String currentUserEmail);

    void activateMeeting(String meetingCode, String currentUserEmail);

    void deactivateMeeting(String meetingCode, String currentUserEmail);

    com.blueant_crm_erp.meeting.dto.response.MeetingResponse processMeetingUpdateWorkflow(String meetingCode, com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest request, String currentUserEmail);

    /**
     * Returns the meetingCode of the latest SCHEDULED meeting for the given lead.
     * Lightweight lookup — used by the frontend to resolve which meeting to act on.
     */
    ActiveMeetingResponse getActiveMeetingByLeadId(String leadId);

    java.util.List<com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse> getMeetingsBySequence(String leadId);

    void convertLead(String leadId, String currentUserEmail);

    com.blueant_crm_erp.meeting.dto.response.MeetingReportResponse getMeetingReports();

}