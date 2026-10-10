package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingVerificationResponse;
import com.blueant_crm_erp.client.service.ClientVerificationService;
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
@RequestMapping("/v1/client-meetings/verification")
@RequiredArgsConstructor
@Tag(name = "Client Meeting Verification API", description = "Endpoints for Process Coordinator Client Meeting Verification")
public class ClientMeetingVerificationController {

    private final ClientVerificationService clientVerificationService;

    @Operation(summary = "Get pending client meeting verifications", description = "Returns client meetings awaiting PC verification")
    @GetMapping("/pending")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<ClientMeetingVerificationResponse>> getPendingVerifications(Pageable pageable) {
        PageResponse<ClientMeetingVerificationResponse> response = clientVerificationService.getPendingVerifications(pageable);
        return ApiResponse.success("Pending client meeting verifications retrieved successfully", response);
    }

    @Operation(summary = "Get verification attempt history", description = "Returns all historical verification attempts for a client meeting")
    @GetMapping("/{meetingCode}/history")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<ClientMeetingVerificationResponse>> getVerificationHistory(@PathVariable String meetingCode) {
        List<ClientMeetingVerificationResponse> history = clientVerificationService.getVerificationHistory(meetingCode);
        return ApiResponse.success("Verification history retrieved successfully", history);
    }

    @Operation(summary = "Verify client meeting", description = "Verifies a conducted or not conducted client meeting with conditional questions")
    @PostMapping("/{meetingCode}/verify")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientMeetingVerificationResponse> verifyMeeting(
            @PathVariable String meetingCode,
            @Valid @RequestBody ClientMeetingVerificationRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientMeetingVerificationResponse response = clientVerificationService.verifyMeeting(meetingCode, request, currentUser);
        return ApiResponse.success("Client meeting verified successfully", response);
    }

    @Operation(summary = "Reject client meeting", description = "Rejects a client meeting with mandatory reason, preserving history for resubmission")
    @PostMapping("/{meetingCode}/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientMeetingVerificationResponse> rejectMeeting(
            @PathVariable String meetingCode,
            @RequestParam String reason,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientMeetingVerificationResponse response = clientVerificationService.rejectMeeting(meetingCode, reason, currentUser);
        return ApiResponse.success("Client meeting rejected successfully", response);
    }
}
