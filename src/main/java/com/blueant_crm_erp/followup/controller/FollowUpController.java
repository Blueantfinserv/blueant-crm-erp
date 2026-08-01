package com.blueant_crm_erp.followup.controller;

import com.blueant_crm_erp.followup.entity.FollowUp;
import com.blueant_crm_erp.followup.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/v1/followups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<com.blueant_crm_erp.followup.dto.response.FollowUpResponse> scheduleFollowUp(
            @RequestParam Long leadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(required = false) String remarks) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        FollowUp followUp = followUpService.scheduleFollowUp(leadId, date, time, remarks, auth.getName());
        return ResponseEntity.ok(org.mapstruct.factory.Mappers.getMapper(com.blueant_crm_erp.followup.mapper.FollowUpMapper.class).toResponse(followUp));
    }

    @PostMapping("/{followUpId}/complete")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<com.blueant_crm_erp.followup.dto.response.FollowUpResponse> completeFollowUp(
            @PathVariable Long followUpId,
            @RequestParam String remarks,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextFollowUpDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime nextFollowUpTime) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        FollowUp followUp = followUpService.completeFollowUp(followUpId, remarks, nextFollowUpDate, nextFollowUpTime, auth.getName());
        return ResponseEntity.ok(org.mapstruct.factory.Mappers.getMapper(com.blueant_crm_erp.followup.mapper.FollowUpMapper.class).toResponse(followUp));
    }
}
