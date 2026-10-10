package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingVerificationResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientVerificationService {

    PageResponse<ClientMeetingVerificationResponse> getPendingVerifications(Pageable pageable);

    List<ClientMeetingVerificationResponse> getVerificationHistory(String meetingCode);

    ClientMeetingVerificationResponse verifyMeeting(String meetingCode, ClientMeetingVerificationRequest request, String currentUserIdentifier);

    ClientMeetingVerificationResponse rejectMeeting(String meetingCode, String reason, String currentUserIdentifier);
}
