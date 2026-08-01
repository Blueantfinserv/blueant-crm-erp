package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
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
    @PreAuthorize("hasRole('PROCESS_COORDINATOR') or hasRole('ADMIN')")
    public ResponseEntity<Meeting> verifyMeeting(
            @PathVariable String meetingCode,
            @RequestParam String remarks) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(processCoordinatorService.verifyMeeting(meetingCode, remarks, auth.getName()));
    }

    @PostMapping("/{meetingCode}/reject")
    @PreAuthorize("hasRole('PROCESS_COORDINATOR') or hasRole('ADMIN')")
    public ResponseEntity<Meeting> rejectMeeting(
            @PathVariable String meetingCode,
            @RequestParam String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(processCoordinatorService.rejectMeeting(meetingCode, reason, auth.getName()));
    }
}
