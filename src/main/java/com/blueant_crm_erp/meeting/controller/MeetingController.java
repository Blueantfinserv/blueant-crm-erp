package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingSearchRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.ActiveMeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDropdownResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingReportResponse;
import com.blueant_crm_erp.meeting.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequestMapping("/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting API", description = "Endpoints for managing meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @PreAuthorize("hasAuthority('MEETING_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new meeting")
    public ApiResponse<MeetingResponse> createMeeting(@Valid @RequestBody CreateMeetingRequest request, Principal principal) {
        log.info("Incoming CreateMeetingRequest: {}", request);
        return ApiResponse.success("Meeting created successfully", meetingService.createMeeting(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('MEETING_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{meetingCode}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update an existing meeting")
    public ApiResponse<MeetingResponse> updateMeeting(@PathVariable String meetingCode,
                                                      @Valid @RequestBody UpdateMeetingRequest request,
                                                      Principal principal) {
        return ApiResponse.success("Meeting updated successfully", meetingService.updateMeeting(meetingCode, request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{meetingCode}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meeting details by code")
    public ApiResponse<MeetingDetailResponse> getMeetingByCode(@PathVariable String meetingCode) {
        return ApiResponse.success(meetingService.getMeetingByCode(meetingCode));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meetings queue based on filters")
    public ApiResponse<List<MeetingResponse>> getAllMeetings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer sequence,
            @RequestParam(required = false) VerificationStatus verificationStatus,
            @RequestParam(required = false) Long salesPersonId,
            @RequestParam(required = false) String salesPersonName) {
        return ApiResponse.success(meetingService.getAllMeetings(search, date, status, sequence, verificationStatus, salesPersonId, salesPersonName));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search meetings")
    public ApiResponse<PageResponse<MeetingSummaryResponse>> searchMeetings(@Valid @RequestBody MeetingSearchRequest request, Pageable pageable) {
        return ApiResponse.success(meetingService.searchMeetings(request, pageable));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/dropdown")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meeting dropdown data")
    public ApiResponse<List<MeetingDropdownResponse>> getMeetingDropdown() {
        return ApiResponse.success(meetingService.getMeetingDropdown());
    }

    @PreAuthorize("hasAuthority('MEETING_DELETE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{meetingCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a meeting")
    public ApiResponse<Void> deleteMeeting(@PathVariable String meetingCode, Principal principal) {
        meetingService.deleteMeeting(meetingCode, principal.getName());
        return ApiResponse.success("Meeting deleted successfully", null);
    }

    @PreAuthorize("hasAuthority('MEETING_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{meetingCode}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Activate a meeting")
    public ApiResponse<Void> activateMeeting(@PathVariable String meetingCode, Principal principal) {
        meetingService.activateMeeting(meetingCode, principal.getName());
        return ApiResponse.success("Meeting activated successfully", null);
    }

    @PreAuthorize("hasAuthority('MEETING_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{meetingCode}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a meeting")
    public ApiResponse<Void> deactivateMeeting(@PathVariable String meetingCode, Principal principal) {
        meetingService.deactivateMeeting(meetingCode, principal.getName());
        return ApiResponse.success("Meeting deactivated successfully", null);
    }

    @PreAuthorize("hasAuthority('MEETING_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{meetingCode}/workflow-update")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Submit meeting workflow update",
        description = "Processes the update of a conducted meeting, updating the meeting status and the lead status based on the outcome."
    )
    public ApiResponse<MeetingResponse> processMeetingWorkflow(
            @PathVariable String meetingCode,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Meeting workflow update details",
                required = true,
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MeetingWorkflowRequest.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Conducted Meeting Update Example",
                        summary = "Example workflow update request",
                        value = "{\n" +
                                "  \"meetingMode\": \"VIRTUAL/ONLINE\",\n" +
                                "  \"aloneWith\": \"SELF\",\n" +
                                "  \"leadStatus\": \"WORK_IN_PROGRESS\",\n" +
                                "  \"meetingRemarks\": \"Discussion went well, client is interested in mutual funds.\",\n" +
                                "  \"nextPlanDate\": \"2026-08-20\"\n" +
                                "}"
                    )
                )
            )
            @Valid @RequestBody MeetingWorkflowRequest request,
            Principal principal) {
        return ApiResponse.success("Meeting workflow processed successfully",
                meetingService.processMeetingUpdateWorkflow(meetingCode, request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/lead/{leadId}/active")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get the active (SCHEDULED) meeting code for a lead",
               description = "Returns meetingCode only. Use the code to call the workflow-update API.")
    public ApiResponse<ActiveMeetingResponse> getActiveMeeting(@PathVariable String leadId, 
                                                               jakarta.servlet.http.HttpServletRequest request, 
                                                               Principal principal) {
        log.info("Incoming leadId:\n{}\nRequest URI:\n{}\nAuthenticated user:\n{}", leadId, request.getRequestURI(), principal != null ? principal.getName() : "Anonymous");
        if (leadId == null || leadId.isBlank() || "undefined".equalsIgnoreCase(leadId.trim())) {
            throw new IllegalArgumentException("Lead ID is required.");
        }
        return ApiResponse.success(meetingService.getActiveMeetingByLeadId(leadId));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/lead/{leadId}/journey")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meetings by sequence for a lead (Journey)")
    public ApiResponse<List<MeetingSummaryResponse>> getMeetingsBySequence(@PathVariable String leadId) {
        return ApiResponse.success(meetingService.getMeetingsBySequence(leadId));
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/lead/{leadId}/history")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meeting history for a lead")
    public ApiResponse<List<MeetingSummaryResponse>> getMeetingHistory(@PathVariable String leadId) {
        // We can reuse getMeetingsBySequence and filter in frontend, or filter here.
        // For simplicity, returning the sequence, but filtered could be done in service.
        List<MeetingSummaryResponse> history = meetingService.getMeetingsBySequence(leadId).stream()
                .filter(m -> com.blueant_crm_erp.meeting.enums.MeetingStatus.COMPLETED.equals(m.getMeetingStatus()))
                .toList();
        return ApiResponse.success(history);
    }

    @PreAuthorize("hasAuthority('MEETING_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/lead/{leadId}/convert")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Explicitly convert a lead")
    public ApiResponse<Void> convertLead(@PathVariable String leadId, Principal principal) {
        meetingService.convertLead(leadId, principal.getName());
        return ApiResponse.success("Lead converted successfully", null);
    }

    @PreAuthorize("hasAuthority('MEETING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/reports")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meeting reports")
    public ApiResponse<MeetingReportResponse> getMeetingReports() {
        return ApiResponse.success(meetingService.getMeetingReports());
    }
}
