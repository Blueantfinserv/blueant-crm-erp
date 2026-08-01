package com.blueant_crm_erp.negotiation.controller;

import com.blueant_crm_erp.negotiation.dto.request.UpdateNegotiationRequest;
import com.blueant_crm_erp.negotiation.dto.response.NegotiationResponse;
import com.blueant_crm_erp.negotiation.entity.Negotiation;
import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import com.blueant_crm_erp.negotiation.exception.NegotiationNotFoundException;
import com.blueant_crm_erp.negotiation.mapper.NegotiationMapper;
import com.blueant_crm_erp.negotiation.repository.NegotiationRepository;
import com.blueant_crm_erp.negotiation.service.NegotiationWorkflowService;
import com.blueant_crm_erp.negotiation.specification.NegotiationSpecification;
import com.blueant_crm_erp.negotiation.validator.NegotiationValidator;
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
@RequestMapping("/v1/negotiations")
@RequiredArgsConstructor
@Tag(name = "Negotiation Controller", description = "Endpoints for managing contract negotiations")
public class NegotiationController {

    private final NegotiationRepository negotiationRepository;
    private final NegotiationWorkflowService negotiationWorkflowService;
    private final NegotiationMapper negotiationMapper;
    private final NegotiationValidator negotiationValidator;

    @Operation(summary = "Get Negotiation by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<NegotiationResponse> getNegotiationById(@PathVariable Long id) {
        return negotiationRepository.findById(id)
                .map(negotiationMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NegotiationNotFoundException("Negotiation not found with ID: " + id));
    }

    @Operation(summary = "Search Negotiations")
    @GetMapping
    @PreAuthorize("hasAuthority('') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<NegotiationResponse>> searchNegotiations(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) NegotiationStatus status,
            @RequestParam(required = false) Long proposalId,
            Pageable pageable) {
        
        Specification<Negotiation> spec = Specification.where(NegotiationSpecification.hasNegotiationCode(code))
                .and(NegotiationSpecification.hasStatus(status))
                .and(NegotiationSpecification.hasProposalId(proposalId));

        return ResponseEntity.ok(negotiationRepository.findAll(spec, pageable).map(negotiationMapper::toResponse));
    }

    @Operation(summary = "Update Negotiation")
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<NegotiationResponse> updateNegotiation(
            @PathVariable String code, 
            @Valid @RequestBody UpdateNegotiationRequest request) {
        
        negotiationValidator.validateUpdateRequest(request);
        
        Negotiation negotiation = negotiationWorkflowService.updateNegotiation(
                code, 
                request.getAgreedAmount(), 
                request.getDiscussion(), 
                SecurityUtil.getCurrentUsername()
        );
        
        return ResponseEntity.ok(negotiationMapper.toResponse(negotiation));
    }

    @Operation(summary = "Close Negotiation")
    @PostMapping("/{code}/close")
    @PreAuthorize("hasAuthority('') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<NegotiationResponse> closeNegotiation(@PathVariable String code) {
        Negotiation negotiation = negotiationWorkflowService.closeNegotiation(code, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(negotiationMapper.toResponse(negotiation));
    }
}
