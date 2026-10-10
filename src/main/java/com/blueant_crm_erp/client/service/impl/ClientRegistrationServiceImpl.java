package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.response.ClientRegistrationResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientCodeGeneratorService;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.client.validator.ClientValidator;
import com.blueant_crm_erp.exception.client.ClientAlreadyExistsException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientValidator clientValidator;
    private final ClientCodeGeneratorService clientCodeGeneratorService;

    @Override
    @Transactional
    public ClientRegistrationResponse registerClient(CreateClientRegistrationRequest request, String currentUserIdentifier) {
        log.info("Registering external client: {}, assigned sales person: {}, by: {}",
                request.getClientName(), request.getSalesPersonEmployeeCode(), currentUserIdentifier);

        // 1. Validate request payload & bestTimeToMeet
        clientValidator.validateRegistration(request);

        // 2. Prevent duplicate client by mobile number
        String normalizedMobile = request.getMobileNumber().trim();
        if (clientRepository.existsByMobileNumber(normalizedMobile)) {
            log.warn("Client registration rejected: Mobile number {} already exists.", normalizedMobile);
            throw new ClientAlreadyExistsException("Client already exists with mobile number: " + normalizedMobile);
        }

        // 3. Resolve target Sales Person and enforce active RM/SC eligibility on backend
        User salesPerson = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(request.getSalesPersonEmployeeCode().trim())
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.getSalesPersonEmployeeCode().trim())
                        .orElseThrow(() -> new ResourceNotFoundException("Sales person not found with code: " + request.getSalesPersonEmployeeCode())));

        clientValidator.validateSalesPersonEligibility(salesPerson);

        // 4. Resolve authenticated onboarding actor
        User assignedBy = resolveUser(currentUserIdentifier);

        // 5. Generate concurrency-safe Client Code
        String clientCode = clientCodeGeneratorService.generateNextClientCode();

        LocalDate assignmentDate = request.getAssignmentDate() != null ? request.getAssignmentDate() : LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 6. Build and persist Client record
        Client client = Client.builder()
                .clientCode(clientCode)
                .clientStatus(ClientStatus.ACTIVE)
                .clientName(request.getClientName().trim())
                .mobileNumber(normalizedMobile)
                .alternateMobileNumber(request.getAlternateMobileNumber() != null ? request.getAlternateMobileNumber().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)
                .speciality(request.getSpeciality().trim())
                .location(request.getLocation().trim())
                .clinicAddress(request.getClinicAddress().trim())
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .assignmentDate(assignmentDate)
                .bestTimeToMeet(request.getBestTimeToMeet() != null ? request.getBestTimeToMeet().trim() : null)
                .salesPerson(salesPerson)
                .createdBySalesPerson(salesPerson)
                .assignedBy(assignedBy)
                .assignedAt(now)
                .clientSince(LocalDate.now())
                .lead(null)
                .build();

        Client savedClient = clientRepository.save(client);

        log.info("External client registered successfully. ClientCode: {}, ID: {}, AssignedTo: {}",
                savedClient.getClientCode(), savedClient.getId(), salesPerson.getEmployeeCode());

        return ClientRegistrationResponse.builder()
                .id(savedClient.getId())
                .clientCode(savedClient.getClientCode())
                .clientName(savedClient.getClientName())
                .mobileNumber(savedClient.getMobileNumber())
                .alternateMobileNumber(savedClient.getAlternateMobileNumber())
                .email(savedClient.getEmail())
                .speciality(savedClient.getSpeciality())
                .location(savedClient.getLocation())
                .clinicAddress(savedClient.getClinicAddress())
                .clientStatus(savedClient.getClientStatus().name())
                .remarks(savedClient.getRemarks())
                .assignmentDate(savedClient.getAssignmentDate())
                .bestTimeToMeet(savedClient.getBestTimeToMeet())
                .assignedSalesPersonId(salesPerson.getId())
                .assignedSalesPersonCode(salesPerson.getEmployeeCode())
                .assignedSalesPersonName(salesPerson.getFullName())
                .assignedSalesPersonRole(SalesRoleResolver.resolveRole(salesPerson).name())
                .assignedBy(assignedBy != null ? assignedBy.getEmployeeCode() : currentUserIdentifier)
                .assignedAt(savedClient.getAssignedAt())
                .createdAt(savedClient.getCreatedAt())
                .createdBy(savedClient.getCreatedBy())
                .build();
    }

    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        return userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier.trim())
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier.trim())
                        .orElse(null));
    }
}
