package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientFollowUpResponse;
import com.blueant_crm_erp.client.dto.response.CrmLeadQueueResponse;
import com.blueant_crm_erp.client.dto.response.CrmVerificationResponse;
import com.blueant_crm_erp.client.service.CrmService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/crm")
@RequiredArgsConstructor
@Tag(name = "CRM Controller", description = "Endpoints for generic CRM processing, questions, verification, and client follow-ups")
public class CrmController {

    private final CrmService crmService;

    @Operation(summary = "Get CRM eligible queue", description = "Returns leads that have been PC-verified as CONVERTED_CLIENT and are ready for CRM processing")
    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('crm:read')")
    public ResponseEntity<ApiResponse<List<CrmLeadQueueResponse>>> getCrmQueue() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        List<CrmLeadQueueResponse> queue = crmService.getCrmQueue(currentUser);
        return ResponseEntity.ok(ApiResponse.success("CRM queue fetched successfully", queue));
    }

    @Operation(summary = "Submit CRM questions & verify lead", description = "Submits questionnaire answers and converts the lead into an active Client assigned to original Sales Person")
    @PostMapping("/leads/{leadCode}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('crm:write')")
    public ResponseEntity<ApiResponse<CrmVerificationResponse>> verifyCrmLead(
            @PathVariable String leadCode,
            @Valid @RequestBody CrmVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        CrmVerificationResponse response = crmService.verifyCrm(leadCode, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("CRM verification completed successfully", response));
    }

    @Operation(summary = "Get ~3-month client follow-ups for Sales Person", description = "Returns clients and their ~3-month follow-up schedules")
    @GetMapping("/clients/follow-ups")
    @PreAuthorize("hasAnyRole('SALES', 'ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('client:read')")
    public ResponseEntity<ApiResponse<List<ClientFollowUpResponse>>> getClientFollowUps(
            @RequestParam(required = false) String salesPersonCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        List<ClientFollowUpResponse> followUps = crmService.getClientFollowUps(salesPersonCode, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Client follow-ups fetched successfully", followUps));
    }
}
