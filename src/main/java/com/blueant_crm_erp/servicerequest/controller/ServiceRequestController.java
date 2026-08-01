package com.blueant_crm_erp.servicerequest.controller;

import com.blueant_crm_erp.servicerequest.dto.request.UpdateServiceRequest;
import com.blueant_crm_erp.servicerequest.dto.response.ServiceRequestResponse;
import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import com.blueant_crm_erp.servicerequest.exception.ServiceRequestNotFoundException;
import com.blueant_crm_erp.servicerequest.mapper.ServiceRequestMapper;
import com.blueant_crm_erp.servicerequest.repository.ServiceRequestRepository;
import com.blueant_crm_erp.servicerequest.service.ServiceRequestWorkflowService;
import com.blueant_crm_erp.servicerequest.specification.ServiceRequestSpecification;
import com.blueant_crm_erp.servicerequest.validator.ServiceRequestValidator;
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
@RequestMapping("/v1/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Request Controller", description = "Endpoints for managing client service requests")
public class ServiceRequestController {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestWorkflowService serviceRequestWorkflowService;
    private final ServiceRequestMapper serviceRequestMapper;
    private final ServiceRequestValidator serviceRequestValidator;

    @Operation(summary = "Get Service Request by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('servicerequest:read') or hasRole('SALES') or hasRole('OPS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Long id) {
        return serviceRequestRepository.findById(id)
                .map(serviceRequestMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ServiceRequestNotFoundException("Service Request not found with ID: " + id));
    }

    @Operation(summary = "Search Service Requests")
    @GetMapping
    @PreAuthorize("hasAuthority('servicerequest:read') or hasRole('SALES') or hasRole('OPS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ServiceRequestResponse>> searchServiceRequests(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) ServiceRequestStatus status,
            @RequestParam(required = false) Long clientId,
            Pageable pageable) {
        
        Specification<ServiceRequest> spec = Specification.where(ServiceRequestSpecification.hasSrCode(code))
                .and(ServiceRequestSpecification.hasStatus(status))
                .and(ServiceRequestSpecification.hasClientId(clientId));

        return ResponseEntity.ok(serviceRequestRepository.findAll(spec, pageable).map(serviceRequestMapper::toResponse));
    }

    @Operation(summary = "Update Service Request")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('servicerequest:write') or hasRole('SALES') or hasRole('OPS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceRequestResponse> updateServiceRequest(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateServiceRequest request) {
        
        serviceRequestValidator.validateUpdateRequest(request);
        
        ServiceRequest sr = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ServiceRequestNotFoundException("Service Request not found with ID: " + id));
                
        serviceRequestMapper.updateEntityFromRequest(request, sr);
        ServiceRequest updated = serviceRequestRepository.save(sr);
        
        return ResponseEntity.ok(serviceRequestMapper.toResponse(updated));
    }

}
