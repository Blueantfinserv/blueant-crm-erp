package com.blueant_crm_erp.lead.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.lead.dto.request.AssignPhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreatePhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.dto.response.PhysicalLeadAssignmentResponse;
import com.blueant_crm_erp.lead.service.PhysicalLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * ============================================================================
 * Physical Lead Controller
 * ============================================================================
 *
 * Controller handling Sales Coordinator Physical Lead management and assignment.
 *
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/Leads_assign")
@RequiredArgsConstructor
public class PhysicalLeadController {

    private final PhysicalLeadService physicalLeadService;

    @PostMapping
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PhysicalLeadAssignmentResponse> createPhysicalLead(
            @Valid @RequestBody CreatePhysicalLeadRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : null;
        return ApiResponse.success("Physical lead created successfully",
                physicalLeadService.createPhysicalLead(request, currentUser));
    }

    @PostMapping("/{leadCode}/assign")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PhysicalLeadAssignmentResponse> assignPhysicalLead(
            @PathVariable String leadCode,
            @Valid @RequestBody AssignPhysicalLeadRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : null;
        return ApiResponse.success("Physical lead assigned successfully",
                physicalLeadService.assignPhysicalLead(leadCode, request, currentUser));
    }

    @GetMapping("/eligible")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<LeadResponse>> getEligiblePhysicalLeads(Pageable pageable) {
        return ApiResponse.success(physicalLeadService.getEligiblePhysicalLeads(pageable));
    }
}
