package com.blueant_crm_erp.lead.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.lead.dto.request.*;
import com.blueant_crm_erp.lead.dto.response.*;
import com.blueant_crm_erp.lead.service.LeadDashboardService;
import com.blueant_crm_erp.lead.service.LeadMeetingVerificationService;
import com.blueant_crm_erp.lead.service.DuplicateTransferApprovalService;
import com.blueant_crm_erp.lead.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final LeadDashboardService dashboardService;
    private final LeadMeetingVerificationService verificationService;
    private final DuplicateTransferApprovalService approvalService;

    @PreAuthorize("hasAuthority('LEAD_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ApiResponse<LeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request, Principal principal) {
        return ApiResponse.success("Lead created successfully", leadService.createLead(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{uniqueLeadId}")
    public ApiResponse<LeadResponse> updateLead(@PathVariable String uniqueLeadId, @Valid @RequestBody UpdateLeadRequest request, Principal principal) {
        return ApiResponse.success("Lead updated successfully", leadService.updateLead(uniqueLeadId, request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/assign")
    public ApiResponse<LeadResponse> assignLead(@Valid @RequestBody AssignLeadRequest request, Principal principal) {
        return ApiResponse.success("Lead assigned successfully", leadService.assignLead(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/transfer")
    public ApiResponse<LeadResponse> transferLead(@Valid @RequestBody TransferLeadRequest request, Principal principal) {
        return ApiResponse.success("Lead transferred successfully", leadService.transferLead(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/status")
    public ApiResponse<LeadResponse> changeStatus(@Valid @RequestBody UpdateLeadStatusRequest request, Principal principal) {
        return ApiResponse.success("Lead status updated", leadService.changeStatus(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/priority")
    public ApiResponse<LeadResponse> changePriority(@Valid @RequestBody ChangeLeadPriorityRequest request, Principal principal) {
        return ApiResponse.success("Lead priority updated", leadService.changePriority(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/convert")
    public ApiResponse<LeadResponse> convertLead(@Valid @RequestBody ConvertLeadRequest request, Principal principal) {
        return ApiResponse.success("Lead converted successfully", leadService.convertLead(request, principal.getName()));
    }

    @PreAuthorize("hasAuthority('LEAD_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{uniqueLeadId}")
    public ApiResponse<LeadDetailResponse> getLeadDetails(@PathVariable String uniqueLeadId) {
        return ApiResponse.success(leadService.getLeadDetails(uniqueLeadId));
    }

    @PreAuthorize("hasAuthority('LEAD_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/search")
    public ApiResponse<PageResponse<LeadResponse>> searchLeads(@RequestBody LeadSearchRequest request, Pageable pageable) {
        return ApiResponse.success(leadService.searchLeads(request, pageable));
    }

    @PreAuthorize("hasAuthority('LEAD_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/filter")
    public ApiResponse<PageResponse<LeadResponse>> filterLeads(@RequestBody LeadFilterRequest request, Pageable pageable) {
        return ApiResponse.success(leadService.filterLeads(request, pageable));
    }

    @PreAuthorize("hasAuthority('LEAD_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/dashboard")
    public ApiResponse<LeadStatisticsResponse> getDashboardSummary() {
        return ApiResponse.success(dashboardService.getDashboardSummary());
    }

    @PreAuthorize("hasAuthority('LEAD_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/statistics")
    public ApiResponse<LeadStatisticsResponse> getStatistics() {
        return ApiResponse.success(dashboardService.getDashboardSummary());
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{uniqueLeadId}/mark-duplicate")
    public ApiResponse<Void> markDuplicate(@PathVariable String uniqueLeadId, Principal principal) {
        leadService.markDuplicate(uniqueLeadId, principal.getName());
        return ApiResponse.success("Lead marked as duplicate", null);
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{uniqueLeadId}/verify-duplicate")
    public ApiResponse<Void> verifyDuplicate(@PathVariable String uniqueLeadId, Principal principal) {
        leadService.verifyDuplicate(uniqueLeadId, principal.getName());
        return ApiResponse.success("Lead duplicate verified", null);
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{uniqueLeadId}/verify-meeting")
    public ApiResponse<Void> verifyMeeting(@PathVariable String uniqueLeadId, Principal principal) {
        verificationService.verifyLeadMeeting(uniqueLeadId, principal.getName());
        return ApiResponse.success("Meeting verified successfully", null);
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{uniqueLeadId}/approve-transfer")
    public ApiResponse<Void> approveTransfer(@PathVariable String uniqueLeadId, Principal principal) {
        approvalService.approveDuplicateTransfer(uniqueLeadId, principal.getName());
        return ApiResponse.success("Transfer approved successfully", null);
    }

    @PreAuthorize("hasAuthority('LEAD_DELETE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{uniqueLeadId}")
    public ApiResponse<Void> deleteLead(@PathVariable String uniqueLeadId, Principal principal) {
        leadService.deleteLead(uniqueLeadId, principal.getName());
        return ApiResponse.success("Lead deleted successfully", null);
    }

    @PreAuthorize("hasAuthority('LEAD_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{uniqueLeadId}/restore")
    public ApiResponse<Void> restoreLead(@PathVariable String uniqueLeadId, Principal principal) {
        leadService.restoreLead(uniqueLeadId, principal.getName());
        return ApiResponse.success("Lead restored successfully", null);
    }
}
