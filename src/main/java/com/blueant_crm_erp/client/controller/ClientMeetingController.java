package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientMeetingRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingResponse;
import com.blueant_crm_erp.client.dto.response.ClientMeetingUpdateResponse;
import com.blueant_crm_erp.client.dto.response.ClientWorkFollowResponse;
import com.blueant_crm_erp.client.service.ClientMeetingService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Client Meeting & Work Follow API", description = "Endpoints for RM/SC Client Work Follow and Client Meetings")
public class ClientMeetingController {

    private final ClientMeetingService clientMeetingService;

    @Operation(summary = "Get Client Work Follow queue", description = "Returns assigned clients for the authenticated RM or SC employee with stable pagination")
    @GetMapping("/v1/clients/work-follow")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<ClientWorkFollowResponse>> getClientWorkFollow(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String salesPersonCode,
            Pageable pageable,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        PageResponse<ClientWorkFollowResponse> response = clientMeetingService.getClientWorkFollow(
                currentUser, pageable, search, status, salesPersonCode);
        return ApiResponse.success("Client work follow queue retrieved successfully", response);
    }

    @Operation(summary = "Schedule Client Meeting", description = "Schedules a new meeting for an assigned client")
    @PostMapping("/v1/client-meetings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientMeetingResponse> scheduleMeeting(
            @Valid @RequestBody CreateClientMeetingRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientMeetingResponse response = clientMeetingService.scheduleMeeting(request, currentUser);
        return ApiResponse.success("Client meeting scheduled successfully", response);
    }

    @Operation(summary = "Submit Client Meeting Update Form", description = "Submits conducted or not conducted meeting update and queues for PC verification")
    @PostMapping("/v1/client-meetings/{meetingCode}/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientMeetingResponse> updateMeeting(
            @PathVariable String meetingCode,
            @Valid @RequestBody ClientMeetingUpdateRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientMeetingResponse response = clientMeetingService.updateMeeting(meetingCode, request, currentUser);
        return ApiResponse.success("Client meeting update submitted successfully", response);
    }

    @Operation(summary = "Get meeting update history", description = "Returns immutable update audit records for a client meeting")
    @GetMapping("/v1/client-meetings/{meetingCode}/update-history")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<ClientMeetingUpdateResponse>> getUpdateHistory(
            @PathVariable String meetingCode,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        List<ClientMeetingUpdateResponse> history = clientMeetingService.getMeetingUpdateHistory(meetingCode, currentUser);
        return ApiResponse.success("Client meeting update history retrieved successfully", history);
    }

    @Operation(summary = "Get client meeting by code", description = "Retrieves details of a client meeting")
    @GetMapping("/v1/client-meetings/{meetingCode}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientMeetingResponse> getMeetingByCode(
            @PathVariable String meetingCode,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientMeetingResponse response = clientMeetingService.getMeetingByCode(meetingCode, currentUser);
        return ApiResponse.success("Client meeting retrieved successfully", response);
    }

    @Operation(summary = "Get client meetings by client code", description = "Retrieves all meetings for a given client")
    @GetMapping("/v1/client-meetings/client/{clientCode}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<ClientMeetingResponse>> getClientMeetings(
            @PathVariable String clientCode,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        List<ClientMeetingResponse> meetings = clientMeetingService.getClientMeetings(clientCode, currentUser);
        return ApiResponse.success("Client meetings retrieved successfully", meetings);
    }
}
