package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/meetings/verification")
@RequiredArgsConstructor
public class ProcessCoordinatorController {

    private final ProcessCoordinatorService processCoordinatorService;

    @PostMapping("/{meetingCode}/verify")
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MeetingResponse> verifyMeeting(
            @PathVariable String meetingCode,
            @Valid @RequestBody MeetingVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(processCoordinatorService.verifyMeeting(meetingCode != null ? meetingCode.trim() : null, request, auth.getName()));
    }

    @PostMapping("/{meetingCode}/reject")
    @PreAuthorize("hasAuthority('MEETING_VERIFY') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MeetingResponse> rejectMeeting(
            @PathVariable String meetingCode,
            @RequestParam String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(processCoordinatorService.rejectMeeting(meetingCode != null ? meetingCode.trim() : null, reason, auth.getName()));
    }

    @GetMapping("/{meetingCode}")
    @PreAuthorize("hasAnyAuthority('MEETING_READ', 'MEETING_VERIFY') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SALES_COORDINATOR')")
    public ResponseEntity<com.blueant_crm_erp.meeting.dto.response.MeetingVerificationResponse> getVerification(
            @PathVariable String meetingCode) {
        return ResponseEntity.ok(processCoordinatorService.getVerification(meetingCode != null ? meetingCode.trim() : null));
    }
}
