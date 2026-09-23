package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.CrmClientAssignRequest;
import com.blueant_crm_erp.client.dto.request.CrmOnboardingRequest;
import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.request.RecordClientFollowUpRequest;
import com.blueant_crm_erp.client.dto.response.*;
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
@Tag(name = "CRM Controller", description = "Endpoints for generic CRM processing, questions 1-28, verification, assignment, and client follow-ups")
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

    @Operation(summary = "Get CRM Onboarding details", description = "Returns onboarding questions 1-28 for a lead eligible for CRM")
    @GetMapping("/onboarding/{leadCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('crm:read')")
    public ResponseEntity<ApiResponse<CrmOnboardingResponse>> getOnboarding(@PathVariable String leadCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        CrmOnboardingResponse response = crmService.getOnboarding(leadCode, currentUser);
        return ResponseEntity.ok(ApiResponse.success("CRM onboarding fetched successfully", response));
    }

    @Operation(summary = "Save or Submit CRM Onboarding (Questions 1-28)", description = "Saves onboarding form. If Payment Done = NO, status remains DRAFT. If Payment Done = YES, status moves to PAYMENT_DONE")
    @PostMapping("/onboarding/{leadCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('crm:write')")
    public ResponseEntity<ApiResponse<CrmOnboardingResponse>> saveOrSubmitOnboarding(
            @PathVariable String leadCode,
            @Valid @RequestBody CrmOnboardingRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        CrmOnboardingResponse response = crmService.saveOrSubmitOnboarding(leadCode, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("CRM onboarding saved successfully", response));
    }

    @Operation(summary = "Submit CRM verification & convert to active Client", description = "Verifies onboarding details, creates Client assigned to original Sales Person, and schedules ~3-month follow-up")
    @PostMapping(value = {"/leads/{leadCode}/verify", "/onboarding/{leadCode}/verify"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM', 'PC_COORDINATOR', 'SALES_COORDINATOR') or hasAuthority('crm:write')")
    public ResponseEntity<ApiResponse<CrmVerificationResponse>> verifyCrmLead(
            @PathVariable String leadCode,
            @Valid @RequestBody CrmVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        CrmVerificationResponse response = crmService.verifyCrm(leadCode, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("CRM verification completed successfully", response));
    }

    @Operation(summary = "Assign Client to Sales Person", description = "Assigns/reassigns Client to a Sales Person while strictly preserving the immutable Created By (original Sales Person)")
    @PostMapping("/clients/{clientCode}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CRM') or hasAuthority('crm:write')")
    public ResponseEntity<ApiResponse<ClientResponse>> assignClient(
            @PathVariable String clientCode,
            @Valid @RequestBody CrmClientAssignRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        ClientResponse response = crmService.assignClient(clientCode, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Client assigned successfully", response));
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

    @Operation(summary = "Record Client Follow-Up", description = "Records completed follow-up and advances next follow-up schedule by ~3 months")
    @PostMapping("/clients/{clientCode}/follow-up")
    @PreAuthorize("hasAnyRole('SALES', 'ADMIN', 'SUPER_ADMIN', 'CRM') or hasAuthority('client:write')")
    public ResponseEntity<ApiResponse<ClientFollowUpRecordResponse>> recordClientFollowUp(
            @PathVariable String clientCode,
            @Valid @RequestBody RecordClientFollowUpRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "system";
        ClientFollowUpRecordResponse response = crmService.recordClientFollowUp(clientCode, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Client follow-up recorded successfully", response));
    }
}
