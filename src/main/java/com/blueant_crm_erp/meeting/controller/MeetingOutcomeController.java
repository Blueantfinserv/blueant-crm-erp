package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.meeting.dto.request.CompleteMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingOutcomeRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.service.MeetingOutcomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Outcome API", description = "Endpoints for meeting outcomes")
public class MeetingOutcomeController {

    private final MeetingOutcomeService meetingOutcomeService;

    @PostMapping("/{meetingCode}/outcome")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update meeting outcome")
    public ApiResponse<MeetingResponse> updateMeetingOutcome(@PathVariable String meetingCode,
                                                             @Valid @RequestBody UpdateMeetingOutcomeRequest request,
                                                             Principal principal) {
        return ApiResponse.success("Meeting outcome updated successfully", meetingOutcomeService.updateMeetingOutcome(meetingCode, request, principal.getName()));
    }

    @PostMapping("/{meetingCode}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Complete meeting with converted status")
    public ApiResponse<MeetingResponse> completeMeeting(@PathVariable String meetingCode,
                                                        @Valid @RequestBody(required = false) CompleteMeetingRequest request,
                                                        Principal principal) {
        return ApiResponse.success("Meeting converted successfully", meetingOutcomeService.markAsConverted(meetingCode, principal.getName()));
    }

    @PostMapping("/{meetingCode}/reject")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark meeting as rejected")
    public ApiResponse<MeetingResponse> rejectMeeting(@PathVariable String meetingCode, Principal principal) {
        return ApiResponse.success("Meeting rejected successfully", meetingOutcomeService.markAsRejected(meetingCode, principal.getName()));
    }

    @GetMapping("/outcome/{outcome}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meetings by outcome")
    public ApiResponse<List<MeetingDetailResponse>> getMeetingsByOutcome(@PathVariable MeetingOutcome outcome) {
        return ApiResponse.success(meetingOutcomeService.getMeetingsByOutcome(outcome));
    }
}
