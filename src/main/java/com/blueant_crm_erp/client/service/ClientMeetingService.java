package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientMeetingRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingResponse;
import com.blueant_crm_erp.client.dto.response.ClientMeetingUpdateResponse;
import com.blueant_crm_erp.client.dto.response.ClientWorkFollowResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientMeetingService {

    PageResponse<ClientWorkFollowResponse> getClientWorkFollow(String currentUserIdentifier, Pageable pageable, String search, String status, String salesPersonCode);

    ClientMeetingResponse scheduleMeeting(CreateClientMeetingRequest request, String currentUserIdentifier);

    ClientMeetingResponse updateMeeting(String meetingCode, ClientMeetingUpdateRequest request, String currentUserIdentifier);

    List<ClientMeetingUpdateResponse> getMeetingUpdateHistory(String meetingCode, String currentUserIdentifier);

    List<ClientMeetingResponse> getClientMeetings(String clientCode, String currentUserIdentifier);

    ClientMeetingResponse getMeetingByCode(String meetingCode, String currentUserIdentifier);
}
