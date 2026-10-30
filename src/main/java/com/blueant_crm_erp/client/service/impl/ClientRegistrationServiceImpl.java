package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.ClientDocumentRequest;
import com.blueant_crm_erp.client.dto.request.ClientNomineeRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientAssignmentHistory;
import com.blueant_crm_erp.client.entity.ClientDocument;
import com.blueant_crm_erp.client.entity.ClientNominee;
import com.blueant_crm_erp.client.enums.ClientBestTimeToMeet;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientAssignmentHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientDocumentRepository;
import com.blueant_crm_erp.client.repository.ClientNomineeRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientCodeGeneratorService;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.client.validator.ClientValidator;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.exception.client.ClientAlreadyExistsException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.blueant_crm_erp.client.enums.ClientDocumentType;

import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyHeadHistory;
import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.client.repository.ClientFamilyHeadHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
import com.blueant_crm_erp.client.service.ActiveInvestmentEligibilityService;
import com.blueant_crm_erp.client.service.FamilyCodeGeneratorService;
import com.blueant_crm_erp.exception.client.FamilyNotFoundException;
import com.blueant_crm_erp.exception.client.IneligibleFamilyHeadException;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientValidator clientValidator;
    private final ClientCodeGeneratorService clientCodeGeneratorService;
    private final ClientNomineeRepository clientNomineeRepository;
    private final ClientDocumentRepository clientDocumentRepository;
    private final ClientAssignmentHistoryRepository clientAssignmentHistoryRepository;
    private final StorageService storageService;
    private final ClientFamilyRepository clientFamilyRepository;
    private final ClientFamilyMemberRepository clientFamilyMemberRepository;
    private final ClientFamilyHeadHistoryRepository clientFamilyHeadHistoryRepository;
    private final FamilyCodeGeneratorService familyCodeGeneratorService;
    private final ActiveInvestmentEligibilityService activeInvestmentEligibilityService;

    @Override
    @Transactional
    public ClientRegistrationResponse registerClient(CreateClientRegistrationRequest request, String currentUserIdentifier) {
        log.info("Registering external client: {}, assigned sales person: {}, by: {}",
                request.getClientName(), request.getSalesPersonEmployeeCode(), currentUserIdentifier);

        // 1. Validate request payload & bestTimeToMeet & profile
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

        // 4. Resolve authenticated onboarding actor and role
        User assignedBy = resolveUser(currentUserIdentifier);
        String createdByRole = resolveCurrentRole();

        // 5. Generate concurrency-safe Client Code
        String clientCode = clientCodeGeneratorService.generateNextClientCode();

        // 5b. Concurrency safety: Re-verify mobile number once sequence lock is acquired
        if (clientRepository.existsByMobileNumber(normalizedMobile)) {
            log.warn("Client registration rejected: Mobile number {} already exists after sequence lock.", normalizedMobile);
            throw new ClientAlreadyExistsException("Client already exists with mobile number: " + normalizedMobile);
        }

        LocalDate assignmentDate = request.getAssignmentDate() != null ? request.getAssignmentDate() : LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 6. Handle investment types compatibility (single vs multiple)
        String investmentType = null;
        if (request.getInvestmentTypes() != null && !request.getInvestmentTypes().isEmpty()) {
            investmentType = String.join(", ", request.getInvestmentTypes());
        } else if (request.getInvestmentType() != null && !request.getInvestmentType().isBlank()) {
            investmentType = request.getInvestmentType().trim();
        }

        String panNumber = request.getPanNumber() != null && !request.getPanNumber().isBlank()
                ? request.getPanNumber().trim().toUpperCase()
                : null;

        // 7. Build and persist Client record
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
                .bestTimeToMeet(request.getBestTimeToMeet() != null ? ClientBestTimeToMeet.normalizeToDisplayName(request.getBestTimeToMeet()) : null)
                .salesPerson(salesPerson)
                .createdBySalesPerson(salesPerson)
                .assignedBy(assignedBy)
                .assignedAt(now)
                .clientSince(LocalDate.now())
                .lead(null)
                // Section A: Personal & Family
                .panNumber(panNumber)
                .dateOfBirth(request.getDateOfBirth())
                .actualDateOfBirth(request.getActualDateOfBirth())
                .motherName(request.getMotherName() != null ? request.getMotherName().trim() : null)
                .placeOfBirth(request.getPlaceOfBirth() != null ? request.getPlaceOfBirth().trim() : null)
                .occupation(request.getOccupation() != null ? request.getOccupation().trim() : null)
                .familyHeadName(request.getFamilyHeadName() != null ? request.getFamilyHeadName().trim() : null)
                .familyDetails(request.getFamilyDetails() != null ? request.getFamilyDetails().trim() : null)
                .residentialAddress(request.getResidentialAddress() != null ? request.getResidentialAddress().trim() : null)
                .correspondenceAddress(request.getCorrespondenceAddress() != null ? request.getCorrespondenceAddress().trim() : null)
                .officeAddress(request.getOfficeAddress() != null ? request.getOfficeAddress().trim() : null)
                .city(request.getCity() != null ? request.getCity().trim() : null)
                .state(request.getState() != null ? request.getState().trim() : null)
                .pinCode(request.getPinCode() != null ? request.getPinCode().trim() : null)
                // Section B: Source & Ownership
                .clientSource(request.getClientSource() != null ? request.getClientSource().trim() : null)
                .sourceDescription(request.getSourceDescription() != null ? request.getSourceDescription().trim() : null)
                .createdByRole(createdByRole)
                .previousSalesPerson(null)
                // Section C: Investment & Application
                .applicationReceivedDate(request.getApplicationReceivedDate())
                .applicationMode(request.getApplicationMode() != null ? request.getApplicationMode().trim() : null)
                .investmentType(investmentType)
                .firstInvestmentAmount(request.getFirstInvestmentAmount())
                .expectedMaxSIP(request.getExpectedMaxSIP())
                .investwellId(request.getInvestwellId() != null ? request.getInvestwellId().trim() : null)
                .clientReportDate(request.getClientReportDate())
                .paymentStatus(request.getPaymentStatus())
                .paymentAmount(request.getPaymentAmount())
                .paymentDate(request.getPaymentDate())
                .paymentReference(request.getPaymentReference() != null ? request.getPaymentReference().trim() : null)
                .build();

        Client savedClient = clientRepository.save(client);

        // 8. Save Nominees (Section D)
        List<ClientNominee> savedNominees = new ArrayList<>();
        if (request.getNominees() != null && !request.getNominees().isEmpty()) {
            for (ClientNomineeRequest nr : request.getNominees()) {
                ClientNominee nominee = ClientNominee.builder()
                        .client(savedClient)
                        .nomineeName(nr.getNomineeName().trim())
                        .relationship(nr.getRelationship().trim())
                        .dateOfBirth(nr.getDateOfBirth())
                        .contactNumber(nr.getContactNumber() != null ? nr.getContactNumber().trim() : null)
                        .panNumber(nr.getPanNumber() != null ? nr.getPanNumber().trim().toUpperCase() : null)
                        .isMinor(Boolean.TRUE.equals(nr.getIsMinor()))
                        .guardianName(nr.getGuardianName() != null ? nr.getGuardianName().trim() : null)
                        .guardianRelationship(nr.getGuardianRelationship() != null ? nr.getGuardianRelationship().trim() : null)
                        .guardianContact(nr.getGuardianContact() != null ? nr.getGuardianContact().trim() : null)
                        .guardianPan(nr.getGuardianPan() != null ? nr.getGuardianPan().trim().toUpperCase() : null)
                        .build();
                savedNominees.add(clientNomineeRepository.save(nominee));
            }
        }

        // 9. Save Document Checklist (Section E)
        List<ClientDocument> savedDocs = new ArrayList<>();
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            for (ClientDocumentRequest dr : request.getDocuments()) {
                String docRef = dr.getDocumentReference() != null ? dr.getDocumentReference().trim() : null;
                if (org.springframework.util.StringUtils.hasText(docRef)) {
                    docRef = storageService.promoteToPermanent(docRef);
                }

                ClientDocument doc = ClientDocument.builder()
                        .client(savedClient)
                        .documentType(dr.getDocumentType())
                        .documentName(dr.getDocumentName() != null ? dr.getDocumentName().trim() : null)
                        .received(Boolean.TRUE.equals(dr.getReceived()))
                        .receivedDate(dr.getReceivedDate())
                        .verificationStatus(dr.getVerificationStatus() != null ? dr.getVerificationStatus() : VerificationStatus.PENDING)
                        .documentReference(docRef)
                        .remarks(dr.getRemarks() != null ? dr.getRemarks().trim() : null)
                        .build();
                savedDocs.add(clientDocumentRepository.save(doc));
            }
        }

        // 10. Record initial Assignment History
        ClientAssignmentHistory history = ClientAssignmentHistory.builder()
                .client(savedClient)
                .assignedFromUser(null)
                .assignedToUser(salesPerson)
                .assignedByUser(assignedBy != null ? assignedBy : salesPerson)
                .assignmentDate(assignmentDate)
                .assignedAt(now)
                .reason("Initial client registration and assignment")
                .build();
        clientAssignmentHistoryRepository.save(history);

        // 11. Handle Family Association (Optional)
        ClientFamily associatedFamily = null;
        if (StringUtils.hasText(request.getFamilyCode())) {
            associatedFamily = clientFamilyRepository.findByFamilyCode(request.getFamilyCode().trim())
                    .orElseThrow(() -> new FamilyNotFoundException("Family not found with code: " + request.getFamilyCode()));

            FamilyRelationship rel = request.getRelationshipToHead() != null
                    ? request.getRelationshipToHead()
                    : FamilyRelationship.OTHER;

            ClientFamilyMember member = ClientFamilyMember.builder()
                    .family(associatedFamily)
                    .client(savedClient)
                    .relationshipToHead(rel)
                    .membershipStatus(FamilyMembershipStatus.ACTIVE)
                    .joinedAt(now)
                    .joinedBy(currentUserIdentifier != null ? currentUserIdentifier : "SYSTEM")
                    .build();
            clientFamilyMemberRepository.save(member);

            savedClient.setFamily(associatedFamily);
            clientRepository.save(savedClient);

            if (Boolean.TRUE.equals(request.getDesignateAsHead())) {
                ActiveInvestmentEligibilityService.EligibilityResult eligibility =
                        activeInvestmentEligibilityService.evaluateEligibility(savedClient);
                if (!eligibility.eligible()) {
                    throw new IneligibleFamilyHeadException(
                            "Cannot designate client " + savedClient.getClientCode() + " as Family Head: " + eligibility.reason()
                    );
                }
                Client prevHead = associatedFamily.getFamilyHead();
                associatedFamily.setFamilyHead(savedClient);
                clientFamilyRepository.save(associatedFamily);

                ClientFamilyHeadHistory headHistory = ClientFamilyHeadHistory.builder()
                        .family(associatedFamily)
                        .previousHead(prevHead)
                        .newHead(savedClient)
                        .changedByUser(currentUserIdentifier != null ? currentUserIdentifier : "SYSTEM")
                        .changedAt(now)
                        .reason("Designated as head during client registration")
                        .build();
                clientFamilyHeadHistoryRepository.save(headHistory);
            }
        } else if (StringUtils.hasText(request.getNewFamilyName())) {
            String generatedFamCode = familyCodeGeneratorService.generateNextFamilyCode();
            ClientFamily newFam = ClientFamily.builder()
                    .familyCode(generatedFamCode)
                    .familyName(request.getNewFamilyName().trim())
                    .status(FamilyStatus.ACTIVE)
                    .build();

            if (Boolean.TRUE.equals(request.getDesignateAsHead())) {
                ActiveInvestmentEligibilityService.EligibilityResult eligibility =
                        activeInvestmentEligibilityService.evaluateEligibility(savedClient);
                if (!eligibility.eligible()) {
                    throw new IneligibleFamilyHeadException(
                            "Cannot designate client " + savedClient.getClientCode() + " as Family Head: " + eligibility.reason()
                    );
                }
                newFam.setFamilyHead(savedClient);
            }

            associatedFamily = clientFamilyRepository.save(newFam);
            savedClient.setFamily(associatedFamily);
            clientRepository.save(savedClient);

            FamilyRelationship rel = request.getRelationshipToHead() != null
                    ? request.getRelationshipToHead()
                    : FamilyRelationship.SELF;

            ClientFamilyMember member = ClientFamilyMember.builder()
                    .family(associatedFamily)
                    .client(savedClient)
                    .relationshipToHead(rel)
                    .membershipStatus(FamilyMembershipStatus.ACTIVE)
                    .joinedAt(now)
                    .joinedBy(currentUserIdentifier != null ? currentUserIdentifier : "SYSTEM")
                    .build();
            clientFamilyMemberRepository.save(member);

            if (Boolean.TRUE.equals(request.getDesignateAsHead())) {
                ClientFamilyHeadHistory headHistory = ClientFamilyHeadHistory.builder()
                        .family(associatedFamily)
                        .previousHead(null)
                        .newHead(savedClient)
                        .changedByUser(currentUserIdentifier != null ? currentUserIdentifier : "SYSTEM")
                        .changedAt(now)
                        .reason("Designated as head during family creation on client registration")
                        .build();
                clientFamilyHeadHistoryRepository.save(headHistory);
            }
        }

        String docStatus = computeDocumentCompletionStatus(savedDocs);

        log.info("External client registered successfully. ClientCode: {}, ID: {}, AssignedTo: {}",
                savedClient.getClientCode(), savedClient.getId(), salesPerson.getEmployeeCode());

        boolean isHead = associatedFamily != null && associatedFamily.getFamilyHead() != null
                && associatedFamily.getFamilyHead().getId().equals(savedClient.getId());

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
                .createdByRole(savedClient.getCreatedByRole())
                .previousSalesPersonId(null)
                .previousSalesPersonCode(null)
                .previousSalesPersonName(null)
                // Section A
                .panNumber(savedClient.getPanNumber())
                .dateOfBirth(savedClient.getDateOfBirth())
                .actualDateOfBirth(savedClient.getActualDateOfBirth())
                .motherName(savedClient.getMotherName())
                .placeOfBirth(savedClient.getPlaceOfBirth())
                .occupation(savedClient.getOccupation())
                .familyHeadName(savedClient.getFamilyHeadName())
                .familyDetails(savedClient.getFamilyDetails())
                .residentialAddress(savedClient.getResidentialAddress())
                .correspondenceAddress(savedClient.getCorrespondenceAddress())
                .officeAddress(savedClient.getOfficeAddress())
                .city(savedClient.getCity())
                .state(savedClient.getState())
                .pinCode(savedClient.getPinCode())
                // Section B
                .clientSource(savedClient.getClientSource())
                .sourceDescription(savedClient.getSourceDescription())
                // Section C
                .applicationReceivedDate(savedClient.getApplicationReceivedDate())
                .applicationMode(savedClient.getApplicationMode())
                .investmentType(savedClient.getInvestmentType())
                .investmentTypes(parseInvestmentTypes(savedClient.getInvestmentType()))
                .firstInvestmentAmount(savedClient.getFirstInvestmentAmount())
                .expectedMaxSIP(savedClient.getExpectedMaxSIP())
                .investwellId(savedClient.getInvestwellId())
                .clientReportDate(savedClient.getClientReportDate())
                .paymentStatus(savedClient.getPaymentStatus())
                .paymentAmount(savedClient.getPaymentAmount())
                .paymentDate(savedClient.getPaymentDate())
                .paymentReference(savedClient.getPaymentReference())
                // Section D & E
                .nominees(savedNominees.stream().map(this::mapNomineeResponse).collect(Collectors.toList()))
                .documents(savedDocs.stream().map(this::mapDocumentResponse).collect(Collectors.toList()))
                .documentCompletionStatus(docStatus)
                // Section F: Family
                .familyId(associatedFamily != null ? associatedFamily.getId() : null)
                .familyCode(associatedFamily != null ? associatedFamily.getFamilyCode() : null)
                .familyName(associatedFamily != null ? associatedFamily.getFamilyName() : null)
                .isFamilyHead(isHead)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(String clientCode, String currentUserIdentifier) {
        Client client = clientRepository.findByClientCode(clientCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with code: " + clientCode));

        List<ClientNominee> nominees = clientNomineeRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        List<ClientDocument> documents = clientDocumentRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        List<ClientAssignmentHistory> histories = clientAssignmentHistoryRepository.findByClientIdOrderByAssignedAtDesc(client.getId());

        String docStatus = computeDocumentCompletionStatus(documents);
        List<String> investmentTypeList = parseInvestmentTypes(client.getInvestmentType());

        User salesPerson = client.getSalesPerson();
        User prevSalesPerson = client.getPreviousSalesPerson();
        User assignedBy = client.getAssignedBy();

        return ClientProfileResponse.builder()
                .id(client.getId())
                .clientCode(client.getClientCode())
                .clientStatus(client.getClientStatus() != null ? client.getClientStatus().name() : null)
                .clientName(client.getClientName())
                .mobileNumber(client.getMobileNumber())
                .alternateMobileNumber(client.getAlternateMobileNumber())
                .email(client.getEmail())
                .speciality(client.getSpeciality())
                .location(client.getLocation())
                .clinicAddress(client.getClinicAddress())
                .clientSince(client.getClientSince())
                .remarks(client.getRemarks())
                .bestTimeToMeet(client.getBestTimeToMeet())
                // Section A
                .panNumber(client.getPanNumber())
                .dateOfBirth(client.getDateOfBirth())
                .actualDateOfBirth(client.getActualDateOfBirth())
                .motherName(client.getMotherName())
                .placeOfBirth(client.getPlaceOfBirth())
                .occupation(client.getOccupation())
                .familyHeadName(client.getFamilyHeadName())
                .familyDetails(client.getFamilyDetails())
                .residentialAddress(client.getResidentialAddress())
                .correspondenceAddress(client.getCorrespondenceAddress())
                .officeAddress(client.getOfficeAddress())
                .city(client.getCity())
                .state(client.getState())
                .pinCode(client.getPinCode())
                // Section B
                .clientSource(client.getClientSource())
                .sourceDescription(client.getSourceDescription())
                .createdByRole(client.getCreatedByRole())
                .previousSalesPersonId(prevSalesPerson != null ? prevSalesPerson.getId() : null)
                .previousSalesPersonCode(prevSalesPerson != null ? prevSalesPerson.getEmployeeCode() : null)
                .previousSalesPersonName(prevSalesPerson != null ? prevSalesPerson.getFullName() : null)
                .assignedSalesPersonId(salesPerson != null ? salesPerson.getId() : null)
                .assignedSalesPersonCode(salesPerson != null ? salesPerson.getEmployeeCode() : null)
                .assignedSalesPersonName(salesPerson != null ? salesPerson.getFullName() : null)
                .assignedSalesPersonRole(salesPerson != null ? SalesRoleResolver.resolveRole(salesPerson).name() : null)
                .assignedBy(assignedBy != null ? assignedBy.getEmployeeCode() : (client.getCreatedBy() != null ? client.getCreatedBy() : currentUserIdentifier))
                .assignedAt(client.getAssignedAt())
                .assignmentDate(client.getAssignmentDate())
                .createdAt(client.getCreatedAt())
                .createdBy(client.getCreatedBy())
                // Section C
                .applicationReceivedDate(client.getApplicationReceivedDate())
                .applicationMode(client.getApplicationMode())
                .investmentType(client.getInvestmentType())
                .investmentTypes(investmentTypeList)
                .firstInvestmentAmount(client.getFirstInvestmentAmount())
                .expectedMaxSIP(client.getExpectedMaxSIP())
                .investwellId(client.getInvestwellId())
                .clientReportDate(client.getClientReportDate())
                .paymentStatus(client.getPaymentStatus())
                .paymentAmount(client.getPaymentAmount())
                .paymentDate(client.getPaymentDate())
                .paymentReference(client.getPaymentReference())
                // Section D
                .nominees(nominees.stream().map(this::mapNomineeResponse).collect(Collectors.toList()))
                // Section E
                .documents(documents.stream().map(this::mapDocumentResponse).collect(Collectors.toList()))
                .documentCompletionStatus(docStatus)
                // History
                .assignmentHistories(histories.stream().map(this::mapAssignmentHistoryResponse).collect(Collectors.toList()))
                // Section F: Family
                .familyId(client.getFamily() != null ? client.getFamily().getId() : null)
                .familyCode(client.getFamily() != null ? client.getFamily().getFamilyCode() : null)
                .familyName(client.getFamily() != null ? client.getFamily().getFamilyName() : null)
                .isFamilyHead(client.getFamily() != null && client.getFamily().getFamilyHead() != null && client.getFamily().getFamilyHead().getId().equals(client.getId()))
                .build();
    }

    private String computeDocumentCompletionStatus(List<ClientDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return "NO_DOCUMENTS";
        }
        Set<ClientDocumentType> distinctReceivedTypes = new HashSet<>();
        boolean allVerified = true;
        boolean allReceived = true;
        boolean anyReceived = false;
        boolean anyRejected = false;

        for (ClientDocument d : documents) {
            if (d.getVerificationStatus() != VerificationStatus.VERIFIED) {
                allVerified = false;
            }
            if (!Boolean.TRUE.equals(d.getReceived())) {
                allReceived = false;
            } else {
                anyReceived = true;
                if (d.getDocumentType() != null && ClientDocumentType.isSupported(d.getDocumentType())) {
                    distinctReceivedTypes.add(d.getDocumentType().toCanonical());
                }
            }
            if (d.getVerificationStatus() == VerificationStatus.REJECTED) {
                anyRejected = true;
            }
        }

        boolean hasTwoDistinctTypes = distinctReceivedTypes.size() >= 2;

        if (hasTwoDistinctTypes && allVerified) return "ALL_VERIFIED";
        if (anyRejected) return "REJECTED";
        if (hasTwoDistinctTypes && allReceived) return "ALL_RECEIVED";
        if (anyReceived) return "PARTIAL";
        return "PENDING";
    }

    private List<String> parseInvestmentTypes(String investmentType) {
        if (investmentType == null || investmentType.isBlank()) {
            return List.of();
        }
        return Arrays.stream(investmentType.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ClientNomineeResponse mapNomineeResponse(ClientNominee nominee) {
        return ClientNomineeResponse.builder()
                .id(nominee.getId())
                .nomineeName(nominee.getNomineeName())
                .relationship(nominee.getRelationship())
                .dateOfBirth(nominee.getDateOfBirth())
                .contactNumber(nominee.getContactNumber())
                .panNumber(nominee.getPanNumber())
                .isMinor(nominee.getIsMinor())
                .guardianName(nominee.getGuardianName())
                .guardianRelationship(nominee.getGuardianRelationship())
                .guardianContact(nominee.getGuardianContact())
                .guardianPan(nominee.getGuardianPan())
                .build();
    }

    private ClientDocumentResponse mapDocumentResponse(ClientDocument doc) {
        return ClientDocumentResponse.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType())
                .documentName(doc.getDocumentName())
                .received(doc.getReceived())
                .receivedDate(doc.getReceivedDate())
                .verificationStatus(doc.getVerificationStatus())
                .documentReference(doc.getDocumentReference())
                .remarks(doc.getRemarks())
                .build();
    }

    private ClientAssignmentHistoryResponse mapAssignmentHistoryResponse(ClientAssignmentHistory hist) {
        User fromUser = hist.getAssignedFromUser();
        User toUser = hist.getAssignedToUser();
        User byUser = hist.getAssignedByUser();

        return ClientAssignmentHistoryResponse.builder()
                .id(hist.getId())
                .clientId(hist.getClient() != null ? hist.getClient().getId() : null)
                .assignedFromUserId(fromUser != null ? fromUser.getId() : null)
                .assignedFromUserCode(fromUser != null ? fromUser.getEmployeeCode() : null)
                .assignedFromUserName(fromUser != null ? fromUser.getFullName() : null)
                .assignedToUserId(toUser != null ? toUser.getId() : null)
                .assignedToUserCode(toUser != null ? toUser.getEmployeeCode() : null)
                .assignedToUserName(toUser != null ? toUser.getFullName() : null)
                .assignedByUserId(byUser != null ? byUser.getId() : null)
                .assignedByUserCode(byUser != null ? byUser.getEmployeeCode() : null)
                .assignedByUserName(byUser != null ? byUser.getFullName() : null)
                .assignmentDate(hist.getAssignmentDate())
                .assignedAt(hist.getAssignedAt())
                .reason(hist.getReason())
                .build();
    }

    private String resolveCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return "CRM_ONBOARDING";
        }
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String authority = ga.getAuthority();
            if (authority.startsWith("ROLE_")) {
                return authority.substring(5);
            }
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("CRM_ONBOARDING");
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
