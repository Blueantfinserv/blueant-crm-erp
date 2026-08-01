package com.blueant_crm_erp.proposal.controller;

import com.blueant_crm_erp.proposal.dto.request.UpdateProposalRequest;
import com.blueant_crm_erp.proposal.dto.response.ProposalResponse;
import com.blueant_crm_erp.proposal.entity.Proposal;
import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import com.blueant_crm_erp.proposal.exception.ProposalNotFoundException;
import com.blueant_crm_erp.proposal.mapper.ProposalMapper;
import com.blueant_crm_erp.proposal.repository.ProposalRepository;
import com.blueant_crm_erp.proposal.service.ProposalWorkflowService;
import com.blueant_crm_erp.proposal.specification.ProposalSpecification;
import com.blueant_crm_erp.proposal.validator.ProposalValidator;
import com.blueant_crm_erp.util.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposal Controller", description = "Endpoints for managing proposals")
public class ProposalController {

    private final ProposalRepository proposalRepository;
    private final ProposalWorkflowService proposalWorkflowService;
    private final ProposalMapper proposalMapper;
    private final ProposalValidator proposalValidator;

    @Operation(summary = "Create Proposal")
    @PostMapping
    @PreAuthorize("hasAuthority('proposal:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProposalResponse> createProposal(@Valid @RequestBody com.blueant_crm_erp.proposal.dto.request.CreateProposalRequest request) {
        Proposal proposal = proposalWorkflowService.createProposal(
                request.getLeadId(),
                request.getInvestmentAmount(),
                request.getProductType(),
                request.getExpectedClosureDate(),
                request.getRemarks(),
                SecurityUtil.getCurrentUsername()
        );
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(proposalMapper.toResponse(proposal));
    }

    @Operation(summary = "Get Proposal by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('proposal:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProposalResponse> getProposalById(@PathVariable Long id) {
        return proposalRepository.findById(id)
                .map(proposalMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProposalNotFoundException("Proposal not found with ID: " + id));
    }

    @Operation(summary = "Search Proposals")
    @GetMapping
    @PreAuthorize("hasAuthority('proposal:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ProposalResponse>> searchProposals(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) Long leadId,
            Pageable pageable) {
        
        Specification<Proposal> spec = Specification.where(ProposalSpecification.hasProposalCode(code))
                .and(ProposalSpecification.hasStatus(status))
                .and(ProposalSpecification.hasLeadId(leadId));

        return ResponseEntity.ok(proposalRepository.findAll(spec, pageable).map(proposalMapper::toResponse));
    }

    @Operation(summary = "Update Proposal")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('proposal:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProposalResponse> updateProposal(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateProposalRequest request) {
        
        proposalValidator.validateUpdateRequest(request);
        
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ProposalNotFoundException("Proposal not found with ID: " + id));
                
        proposalMapper.updateEntityFromRequest(request, proposal);
        Proposal updated = proposalRepository.save(proposal);
        
        return ResponseEntity.ok(proposalMapper.toResponse(updated));
    }

    @Operation(summary = "Accept Proposal")
    @PostMapping("/{code}/accept")
    @PreAuthorize("hasAuthority('proposal:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProposalResponse> acceptProposal(@PathVariable String code) {
        Proposal proposal = proposalWorkflowService.acceptProposal(code, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(proposalMapper.toResponse(proposal));
    }

    @Operation(summary = "Reject Proposal")
    @PostMapping("/{code}/reject")
    @PreAuthorize("hasAuthority('proposal:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProposalResponse> rejectProposal(@PathVariable String code, @RequestParam String reason) {
        Proposal proposal = proposalWorkflowService.rejectProposal(code, reason, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(proposalMapper.toResponse(proposal));
    }
}
