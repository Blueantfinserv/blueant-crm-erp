package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ClientNomineeRequest;
import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.dto.response.ClientResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.entity.ClientNominee;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.client.exception.ClientNotFoundException;
import com.blueant_crm_erp.client.mapper.ClientMapper;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.FamilyService;
import com.blueant_crm_erp.client.specification.ClientSpecification;
import com.blueant_crm_erp.client.validator.ClientValidator;
import com.blueant_crm_erp.exception.common.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Controller", description = "Endpoints for managing converted clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientFamilyMemberRepository clientFamilyMemberRepository;
    private final FamilyService familyService;
    private final ClientMapper clientMapper;
    private final ClientValidator clientValidator;

    @Operation(summary = "Get Client by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + id));
    }

    @Operation(summary = "Search Clients")
    @GetMapping
    @PreAuthorize("hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @RequestParam(required = false) String clientCode,
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(required = false) Long rmId,
            Pageable pageable) {
        
        Specification<Client> spec = Specification.where(ClientSpecification.hasClientCode(clientCode))
                .and(ClientSpecification.hasStatus(status))
                .and(ClientSpecification.hasRelationshipManager(rmId));

        return ResponseEntity.ok(clientRepository.findAll(spec, pageable).map(clientMapper::toResponse));
    }

    @Operation(summary = "Update Client")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CRM_ONBOARDING', 'CRM', 'ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request,
            Authentication authentication) {
        clientValidator.validateUpdateRequest(request);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + id));

        Authentication auth = authentication != null ? authentication : SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin) {
            validateCrmUpdatePermissions(client, request);
        }

        // Handle investmentTypes array
        if (request.getInvestmentTypes() != null && !request.getInvestmentTypes().isEmpty()) {
            client.setInvestmentType(String.join(", ", request.getInvestmentTypes()));
        }

        // Handle nominees update
        if (request.getNominees() != null) {
            client.getNominees().clear();
            for (ClientNomineeRequest nr : request.getNominees()) {
                if (nr.getNomineeName() != null && !nr.getNomineeName().isBlank()) {
                    ClientNominee nominee = ClientNominee.builder()
                            .client(client)
                            .nomineeName(nr.getNomineeName().trim())
                            .relationship(nr.getRelationship() != null ? nr.getRelationship().trim() : "")
                            .dateOfBirth(nr.getDateOfBirth())
                            .contactNumber(nr.getContactNumber() != null ? nr.getContactNumber().trim() : null)
                            .panNumber(nr.getPanNumber() != null ? nr.getPanNumber().trim().toUpperCase() : null)
                            .isMinor(Boolean.TRUE.equals(nr.getIsMinor()))
                            .guardianName(nr.getGuardianName() != null ? nr.getGuardianName().trim() : null)
                            .guardianRelationship(nr.getGuardianRelationship() != null ? nr.getGuardianRelationship().trim() : null)
                            .guardianContact(nr.getGuardianContact() != null ? nr.getGuardianContact().trim() : null)
                            .guardianPan(nr.getGuardianPan() != null ? nr.getGuardianPan().trim().toUpperCase() : null)
                            .build();
                    client.getNominees().add(nominee);
                }
            }
        }

        // Handle family association
        if (StringUtils.hasText(request.getFamilyCode())) {
            String targetFamilyCode = request.getFamilyCode().trim();
            Optional<ClientFamilyMember> activeMembership = clientFamilyMemberRepository
                    .findByClientIdAndMembershipStatus(client.getId(), FamilyMembershipStatus.ACTIVE);

            if (activeMembership.isPresent()) {
                ClientFamily currentFamily = activeMembership.get().getFamily();
                if (currentFamily != null && targetFamilyCode.equalsIgnoreCase(currentFamily.getFamilyCode())) {
                    // Already an active member of this family; preserve membership and prevent duplicate records
                    client.setFamily(currentFamily);
                } else {
                    // Client belongs to another active family; familyService.addMember enforces cross-family transfer rules (throws FamilyConflictException)
                    AddFamilyMemberRequest addMemberRequest = AddFamilyMemberRequest.builder()
                            .clientId(client.getId())
                            .relationshipToHead(FamilyRelationship.OTHER)
                            .build();
                    familyService.addMember(targetFamilyCode, addMemberRequest, auth != null ? auth.getName() : "SYSTEM");
                    clientFamilyMemberRepository.findByClientIdAndMembershipStatus(client.getId(), FamilyMembershipStatus.ACTIVE)
                            .ifPresent(m -> client.setFamily(m.getFamily()));
                }
            } else {
                // Client has no active family membership; link via familyService to create ClientFamilyMember
                AddFamilyMemberRequest addMemberRequest = AddFamilyMemberRequest.builder()
                        .clientId(client.getId())
                        .relationshipToHead(FamilyRelationship.OTHER)
                        .build();
                familyService.addMember(targetFamilyCode, addMemberRequest, auth != null ? auth.getName() : "SYSTEM");
                clientFamilyMemberRepository.findByClientIdAndMembershipStatus(client.getId(), FamilyMembershipStatus.ACTIVE)
                        .ifPresent(m -> client.setFamily(m.getFamily()));
            }
        }

        clientMapper.updateEntityFromRequest(request, client);
        Client updated = clientRepository.save(client);
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    private void validateCrmUpdatePermissions(Client client, UpdateClientRequest request) {
        if (request.getClientName() != null && !request.getClientName().trim().equalsIgnoreCase(client.getClientName())) {
            throw new AccessDeniedException("CRM is not authorized to modify client name.");
        }
        if (request.getSpeciality() != null && !request.getSpeciality().trim().equalsIgnoreCase(client.getSpeciality())) {
            throw new AccessDeniedException("CRM is not authorized to modify speciality.");
        }
        if (request.getMobileNumber() != null && !request.getMobileNumber().trim().equals(client.getMobileNumber())) {
            throw new AccessDeniedException("CRM is not authorized to modify mobile number.");
        }
        if (request.getLocation() != null && (client.getLocation() == null || !request.getLocation().trim().equalsIgnoreCase(client.getLocation()))) {
            throw new AccessDeniedException("CRM is not authorized to modify client location.");
        }
        if (request.getClinicAddress() != null && (client.getClinicAddress() == null || !request.getClinicAddress().trim().equalsIgnoreCase(client.getClinicAddress()))) {
            throw new AccessDeniedException("CRM is not authorized to modify clinic address.");
        }
        if (request.getClientStatus() != null && !request.getClientStatus().equals(client.getClientStatus())) {
            throw new AccessDeniedException("CRM is not authorized to modify client status.");
        }
        if (request.getEmail() != null && (client.getEmail() == null || !request.getEmail().trim().equalsIgnoreCase(client.getEmail()))) {
            throw new AccessDeniedException("CRM is not authorized to modify client email.");
        }
        if (request.getAlternateMobileNumber() != null && (client.getAlternateMobileNumber() == null || !request.getAlternateMobileNumber().trim().equals(client.getAlternateMobileNumber()))) {
            throw new AccessDeniedException("CRM is not authorized to modify alternate mobile number.");
        }
        if (request.getRemarks() != null && (client.getRemarks() == null || !request.getRemarks().trim().equals(client.getRemarks()))) {
            throw new AccessDeniedException("CRM is not authorized to modify client remarks.");
        }
        if (request.getBestTimeToMeet() != null && (client.getBestTimeToMeet() == null || !request.getBestTimeToMeet().trim().equalsIgnoreCase(client.getBestTimeToMeet()))) {
            throw new AccessDeniedException("CRM is not authorized to modify best time to meet.");
        }
        if (request.getAmc() != null && (client.getAmc() == null || !request.getAmc().trim().equalsIgnoreCase(client.getAmc()))) {
            throw new AccessDeniedException("CRM is not authorized to modify AMC.");
        }
        if (request.getScheme() != null && (client.getScheme() == null || !request.getScheme().trim().equalsIgnoreCase(client.getScheme()))) {
            throw new AccessDeniedException("CRM is not authorized to modify scheme.");
        }
        if (request.getAssignmentDate() != null && (client.getAssignmentDate() == null || !request.getAssignmentDate().equals(client.getAssignmentDate()))) {
            throw new AccessDeniedException("CRM is not authorized to modify assignment date.");
        }
        if (request.getClientSince() != null && (client.getClientSince() == null || !request.getClientSince().equals(client.getClientSince()))) {
            throw new AccessDeniedException("CRM is not authorized to modify client since date.");
        }
        if (request.getRelationshipManagerId() != null) {
            boolean matchesSalesPerson = client.getSalesPerson() != null && request.getRelationshipManagerId().equals(client.getSalesPerson().getId());
            boolean matchesRm = client.getRelationshipManager() != null && request.getRelationshipManagerId().equals(client.getRelationshipManager().getId());
            if (!matchesSalesPerson && !matchesRm) {
                throw new AccessDeniedException("CRM is not authorized to modify relationship manager.");
            }
        }
        if (request.getCrmOwnerId() != null) {
            boolean matchesCrmOwner = client.getCrmOwner() != null && request.getCrmOwnerId().equals(client.getCrmOwner().getId());
            boolean matchesAssignedBy = client.getAssignedBy() != null && request.getCrmOwnerId().equals(client.getAssignedBy().getId());
            if (!matchesCrmOwner && !matchesAssignedBy) {
                throw new AccessDeniedException("CRM is not authorized to modify CRM owner.");
            }
        }
    }
}
