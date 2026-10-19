package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ChangeFamilyHeadRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.client.service.FamilyService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/client-families")
@RequiredArgsConstructor
@Tag(name = "Client Family Management API", description = "Endpoints for managing client families and CRM-controlled family heads")
public class ClientFamilyController {

    private final FamilyService familyService;

    @Operation(summary = "Create family", description = "Creates a new client family, optionally designating an eligible initial head")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<FamilyDetailResponse> createFamily(
            @Valid @RequestBody CreateFamilyRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        FamilyDetailResponse response = familyService.createFamily(request, currentUser);
        return ApiResponse.success("Family created successfully", response);
    }

    @Operation(summary = "List families", description = "Search and list families with pagination and filters")
    @GetMapping
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Page<FamilySummaryResponse>> getFamilies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FamilyStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FamilySummaryResponse> response = familyService.getFamilies(pageable, search, status);
        return ApiResponse.success("Families retrieved successfully", response);
    }

    @Operation(summary = "Get family details", description = "Retrieves family details along with active member summaries")
    @GetMapping("/{familyCode}")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<FamilyDetailResponse> getFamilyByCode(@PathVariable String familyCode) {
        FamilyDetailResponse response = familyService.getFamilyByCode(familyCode);
        return ApiResponse.success("Family retrieved successfully", response);
    }

    @Operation(summary = "Add member to family", description = "Links an existing client to a family")
    @PostMapping("/{familyCode}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<FamilyMemberResponse> addMember(
            @PathVariable String familyCode,
            @Valid @RequestBody AddFamilyMemberRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        FamilyMemberResponse response = familyService.addMember(familyCode, request, currentUser);
        return ApiResponse.success("Member added to family successfully", response);
    }

    @Operation(summary = "List eligible family heads", description = "Returns active family members with their active investment eligibility status")
    @GetMapping("/{familyCode}/eligible-heads")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<EligibleFamilyHeadResponse>> getEligibleHeads(@PathVariable String familyCode) {
        List<EligibleFamilyHeadResponse> response = familyService.getEligibleHeads(familyCode);
        return ApiResponse.success("Eligible family heads evaluated successfully", response);
    }

    @Operation(summary = "Change family head", description = "Explicitly sets or changes the family head with mandatory active investment validation and concurrency lock")
    @PutMapping("/{familyCode}/head")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<FamilyDetailResponse> changeFamilyHead(
            @PathVariable String familyCode,
            @Valid @RequestBody ChangeFamilyHeadRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        FamilyDetailResponse response = familyService.changeFamilyHead(familyCode, request, currentUser);
        return ApiResponse.success("Family head updated successfully", response);
    }

    @Operation(summary = "Get family head change history", description = "Returns immutable audit history of all family head changes")
    @GetMapping("/{familyCode}/head-history")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<FamilyHeadHistoryResponse>> getFamilyHeadHistory(@PathVariable String familyCode) {
        List<FamilyHeadHistoryResponse> response = familyService.getFamilyHeadHistory(familyCode);
        return ApiResponse.success("Family head history retrieved successfully", response);
    }
}
