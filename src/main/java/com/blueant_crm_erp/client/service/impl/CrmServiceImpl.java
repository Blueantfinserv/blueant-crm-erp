package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.CrmClientAssignRequest;
import com.blueant_crm_erp.client.dto.request.CrmOnboardingRequest;
import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.request.RecordClientFollowUpRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFollowUp;
import com.blueant_crm_erp.client.entity.CrmOnboarding;
import com.blueant_crm_erp.client.entity.CrmVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.CrmOnboardingStatus;
import com.blueant_crm_erp.client.mapper.ClientMapper;
import com.blueant_crm_erp.client.repository.ClientFollowUpRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.repository.CrmOnboardingRepository;
import com.blueant_crm_erp.client.repository.CrmVerificationRepository;
import com.blueant_crm_erp.client.service.CrmService;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrmServiceImpl implements CrmService {

    private final LeadRepository leadRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingVerificationRepository meetingVerificationRepository;
    private final CrmVerificationRepository crmVerificationRepository;
    private final CrmOnboardingRepository crmOnboardingRepository;
    private final ClientRepository clientRepository;
    private final ClientFollowUpRepository clientFollowUpRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CrmLeadQueueResponse> getCrmQueue(String currentUserEmail) {
        log.info("Fetching CRM queue for user: {}", currentUserEmail);

        // Fetch leads that are potential conversion candidates
        List<Lead> candidateLeads = new ArrayList<>(leadRepository.findAllByLeadStatus(LeadStatus.CONVERTED));

        List<CrmLeadQueueResponse> queue = new ArrayList<>();

        for (Lead lead : candidateLeads) {
            // Check if already CRM-verified
            boolean alreadyVerified = crmVerificationRepository.existsByLeadIdAndVerificationStatus(
                    lead.getId(), VerificationStatus.VERIFIED);
            if (alreadyVerified) {
                continue;
            }

            // CRITICAL BUSINESS RULE: CRM must ONLY receive the lead AFTER PC Meeting Verification
            Optional<MeetingVerification> pcVerificationOpt = meetingVerificationRepository
                    .findTopByMeetingLeadIdAndVerificationStatusOrderByMeetingMeetingNumberDesc(
                            lead.getId(), VerificationStatus.VERIFIED);

            if (pcVerificationOpt.isEmpty()) {
                // Not verified by PC yet -> strictly excluded from CRM
                continue;
            }

            MeetingVerification pcVerification = pcVerificationOpt.get();
            Meeting meeting = pcVerification.getMeeting();

            // Verify that the meeting outcome was CONVERTED_CLIENT
            if (meeting.getLeadStatus() != MeetingLeadStatus.CONVERTED_CLIENT) {
                continue;
            }

            User salesPerson = lead.getAssignedSalesPerson();
            if (salesPerson == null && meeting.getAssignedEmployee() != null) {
                salesPerson = meeting.getAssignedEmployee();
            }

            // Check onboarding status if exists
            Optional<CrmOnboarding> onboardingOpt = crmOnboardingRepository.findByLeadId(lead.getId());
            String onboardingStatus = onboardingOpt.map(o -> o.getOnboardingStatus().name()).orElse("NOT_STARTED");

            CrmLeadQueueResponse response = CrmLeadQueueResponse.builder()
                    .leadId(lead.getId())
                    .leadCode(lead.getLeadCode())
                    .uniqueLeadId(lead.getUniqueLeadId())
                    .clientName(lead.getClientName())
                    .mobileNumber(lead.getMobileNumber())
                    .email(lead.getEmail())
                    .location(lead.getLocation())
                    .salesPersonId(salesPerson != null ? salesPerson.getId() : null)
                    .salesPersonCode(salesPerson != null ? salesPerson.getEmployeeCode() : null)
                    .salesPersonName(salesPerson != null ? salesPerson.getFullName() : null)
                    .verifiedMeetingCode(meeting.getMeetingCode())
                    .pcVerifiedAt(pcVerification.getVerifiedAt())
                    .pcVerifiedBy(pcVerification.getVerifiedBy())
                    .leadStatus(lead.getLeadStatus() != null ? lead.getLeadStatus().name() : null)
                    .leadStage(lead.getLeadStage() != null ? lead.getLeadStage().name() : null)
                    .crmOnboardingStatus(onboardingStatus)
                    .crmVerificationStatus(VerificationStatus.PENDING.name())
                    .build();

            queue.add(response);
        }

        return queue;
    }

    @Override
    @Transactional(readOnly = true)
    public CrmOnboardingResponse getOnboarding(String leadCode, String currentUserEmail) {
        log.info("Fetching CRM onboarding for lead: {}, by: {}", leadCode, currentUserEmail);

        Lead lead = findLeadByCodeOrThrow(leadCode);
        validateLeadEligibleForCrm(lead);

        Optional<CrmOnboarding> onboardingOpt = crmOnboardingRepository.findByLeadId(lead.getId());
        if (onboardingOpt.isPresent()) {
            return mapToOnboardingResponse(onboardingOpt.get());
        }

        // Pre-fill with lead information and original sales person context
        User originalSalesPerson = resolveOriginalSalesPerson(lead);

        return CrmOnboardingResponse.builder()
                .leadId(lead.getId())
                .leadCode(lead.getLeadCode())
                .onboardingStatus("NOT_STARTED")
                .createdBySalesPersonId(originalSalesPerson != null ? originalSalesPerson.getId() : null)
                .createdBySalesPersonCode(originalSalesPerson != null ? originalSalesPerson.getEmployeeCode() : null)
                .createdBySalesPersonName(originalSalesPerson != null ? originalSalesPerson.getFullName() : null)
                .investorName(lead.getClientName())
                .contactDetail(lead.getMobileNumber())
                .mailId(lead.getEmail())
                .location(lead.getLocation())
                .source(lead.getLeadSource() != null ? lead.getLeadSource().name() : null)
                .sourceDescription(lead.getClinicAddress())
                .paymentDone(Boolean.FALSE)
                .isBlueantInvestor(Boolean.FALSE)
                .allDocumentsCompleted(Boolean.FALSE)
                .build();
    }

    @Override
    public CrmOnboardingResponse saveOrSubmitOnboarding(String leadCode, CrmOnboardingRequest request, String currentUserEmail) {
        log.info("Saving/Submitting CRM onboarding for lead: {}, by: {}", leadCode, currentUserEmail);

        Lead lead = findLeadByCodeOrThrow(leadCode);
        validateLeadEligibleForCrm(lead);

        // Resolve or preserve original Sales Person (Question 5: Created By)
        CrmOnboarding onboarding = crmOnboardingRepository.findByLeadId(lead.getId())
                .orElseGet(() -> {
                    User originalSp = resolveOriginalSalesPerson(lead);
                    if (originalSp == null && request.getCreatedBySalesPersonCode() != null) {
                        originalSp = userRepository.findByEmployeeCodeIgnoreCase(request.getCreatedBySalesPersonCode()).orElse(null);
                    }
                    if (originalSp == null) {
                        originalSp = userRepository.findByEmailIgnoreCaseAndDeletedFalse(currentUserEmail).orElse(null);
                    }
                    if (originalSp == null) {
                        throw new IllegalStateException("Original Sales Person cannot be determined for lead: " + leadCode);
                    }
                    return CrmOnboarding.builder()
                            .lead(lead)
                            .createdBySalesPerson(originalSp)
                            .build();
                });

        // Question 28: Payment Done Decision Point
        boolean isPaymentDone = Boolean.TRUE.equals(request.getPaymentDone());
        if (isPaymentDone) {
            onboarding.setOnboardingStatus(CrmOnboardingStatus.PAYMENT_DONE);
        } else {
            // IF Payment Done = NO -> record remains in DRAFT state
            onboarding.setOnboardingStatus(CrmOnboardingStatus.DRAFT);
        }

        // Map Questions 1 to 28
        onboarding.setInvestorName(request.getInvestorName() != null && !request.getInvestorName().isBlank()
                ? request.getInvestorName().trim() : lead.getClientName());
        onboarding.setIsBlueantInvestor(Boolean.TRUE.equals(request.getIsBlueantInvestor()));
        onboarding.setFamilyHead(request.getFamilyHead());
        onboarding.setOccupation(request.getOccupation());
        // Question 5 is mapped by createdBySalesPerson (never overwritten)
        onboarding.setPanNumber(request.getPanNumber() != null ? request.getPanNumber().trim() : null);
        onboarding.setContactDetail(request.getContactDetail() != null ? request.getContactDetail().trim() : lead.getMobileNumber());
        onboarding.setMailId(request.getMailId() != null ? request.getMailId().trim() : lead.getEmail());
        onboarding.setCorrespondenceAddress(request.getCorrespondenceAddress());
        onboarding.setOfficeAddress(request.getOfficeAddress());
        onboarding.setPlaceOfBirth(request.getPlaceOfBirth());
        onboarding.setFamilyDetails(request.getFamilyDetails());
        onboarding.setLocation(request.getLocation() != null ? request.getLocation() : lead.getLocation());
        onboarding.setSource(request.getSource() != null ? request.getSource()
                : (lead.getLeadSource() != null ? lead.getLeadSource().name() : null));
        onboarding.setSourceDescription(request.getSourceDescription());
        onboarding.setApplicationReceivedDate(request.getApplicationReceivedDate() != null
                ? request.getApplicationReceivedDate() : LocalDate.now());
        onboarding.setNomineeDetails(request.getNomineeDetails());
        onboarding.setNomineePanOrAadhaar(request.getNomineePanOrAadhaar());
        onboarding.setMotherName(request.getMotherName());
        onboarding.setApplicationMode(request.getApplicationMode());
        onboarding.setFirstInvestmentAmount(request.getFirstInvestmentAmount());
        onboarding.setExpectedMaxSIP(request.getExpectedMaxSIP());
        onboarding.setInvestmentType(request.getInvestmentType());
        onboarding.setAllDocumentsCompleted(Boolean.TRUE.equals(request.getAllDocumentsCompleted()));
        onboarding.setInvestwellUserId(request.getInvestwellUserId());
        onboarding.setHelpdeskQueryNo(request.getHelpdeskQueryNo());
        onboarding.setClientReportedDate(request.getClientReportedDate() != null
                ? request.getClientReportedDate() : LocalDate.now());
        onboarding.setPaymentDone(isPaymentDone);

        onboarding.setRemarks(request.getRemarks());
        onboarding.setSubmittedAt(LocalDateTime.now());
        onboarding.setSubmittedBy(currentUserEmail);

        CrmOnboarding saved = crmOnboardingRepository.save(onboarding);
        log.info("CRM Onboarding saved for lead: {}. PaymentDone: {}, Status: {}",
                lead.getLeadCode(), isPaymentDone, saved.getOnboardingStatus());

        return mapToOnboardingResponse(saved);
    }

    @Override
    public CrmVerificationResponse verifyCrm(String leadCode, CrmVerificationRequest request, String currentUserEmail) {
        log.info("Processing CRM verification for lead: {}, by: {}", leadCode, currentUserEmail);

        Lead lead = findLeadByCodeOrThrow(leadCode);
        validateLeadEligibleForCrm(lead);

        // Enforce Question 28 Payment Done Decision Point:
        // Only after Payment Done = YES can proceed to CRM Verification -> CLIENT.
        // If Payment Done = NO, the record must remain in DRAFT state.
        Optional<CrmOnboarding> onboardingOpt = crmOnboardingRepository.findByLeadId(lead.getId());
        CrmOnboarding onboarding;

        if (onboardingOpt.isPresent()) {
            onboarding = onboardingOpt.get();
            if (!Boolean.TRUE.equals(onboarding.getPaymentDone())) {
                throw new IllegalStateException(
                        "Cannot verify client: Payment is not done. Onboarding record is in DRAFT state.");
            }
        } else {
            // Backward compatibility for direct verification calls without separate onboarding step
            User originalSp = resolveOriginalSalesPerson(lead);
            if (originalSp == null) {
                originalSp = userRepository.findByEmailIgnoreCaseAndDeletedFalse(currentUserEmail).orElse(null);
            }
            onboarding = CrmOnboarding.builder()
                    .lead(lead)
                    .createdBySalesPerson(originalSp)
                    .investorName(lead.getClientName())
                    .contactDetail(lead.getMobileNumber())
                    .mailId(lead.getEmail())
                    .location(lead.getLocation())
                    .source(lead.getLeadSource() != null ? lead.getLeadSource().name() : null)
                    .paymentDone(Boolean.TRUE)
                    .onboardingStatus(CrmOnboardingStatus.PAYMENT_DONE)
                    .submittedAt(LocalDateTime.now())
                    .submittedBy(currentUserEmail)
                    .build();
            onboarding = crmOnboardingRepository.save(onboarding);
        }

        // Populate CRM Verification Entity
        CrmVerification crmVerification = crmVerificationRepository.findByLeadId(lead.getId())
                .orElseGet(() -> CrmVerification.builder().lead(lead).build());

        crmVerification.setCrmOnboarding(onboarding);
        crmVerification.setVerificationStatus(VerificationStatus.VERIFIED);
        crmVerification.setKycVerified(Boolean.TRUE.equals(request.getKycVerified()));
        crmVerification.setBankDetailsVerified(Boolean.TRUE.equals(request.getBankDetailsVerified()));
        crmVerification.setDocumentsVerified(Boolean.TRUE.equals(request.getDocumentsVerified()));
        crmVerification.setClientContactConfirmed(Boolean.TRUE.equals(request.getClientContactConfirmed()));
        crmVerification.setPanNumber(request.getPanNumber() != null ? request.getPanNumber().trim() : onboarding.getPanNumber());
        crmVerification.setRemarks(request.getRemarks());
        crmVerification.setVerifiedBy(currentUserEmail);
        crmVerification.setVerifiedAt(LocalDateTime.now());
        crmVerification.setRejectionReason(null);

        // Resolve Original Sales Person (Created By)
        User originalSalesPerson = onboarding.getCreatedBySalesPerson();
        if (originalSalesPerson == null) {
            originalSalesPerson = resolveOriginalSalesPerson(lead);
        }

        // Schedule initial ~3-month follow-up
        LocalDate nextFollowupDate = LocalDate.now().plusMonths(3);

        // Create / Activate Client entity:
        // Set Created By (Original Sales Person) and Assigned To (Initial Sales Person)
        Client client = clientRepository.findByLeadId(lead.getId())
                .orElseGet(() -> Client.builder()
                        .lead(lead)
                        .clientCode("CLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .build());

        client.setClientStatus(ClientStatus.ACTIVE);
        client.setClientName(onboarding.getInvestorName() != null ? onboarding.getInvestorName() : lead.getClientName());
        client.setMobileNumber(onboarding.getContactDetail() != null ? onboarding.getContactDetail() : lead.getMobileNumber());
        client.setEmail(onboarding.getMailId() != null ? onboarding.getMailId() : lead.getEmail());

        String pan = request.getPanNumber() != null && !request.getPanNumber().isBlank()
                ? request.getPanNumber().trim() : onboarding.getPanNumber();
        if (pan != null && !pan.isBlank()) {
            client.setPanNumber(pan);
        }

        if (onboarding.getInvestmentType() != null) {
            client.setInvestmentType(onboarding.getInvestmentType());
        }

        // CRITICAL: Created By = Original Sales Person (NEVER overwritten)
        if (client.getCreatedBySalesPerson() == null) {
            client.setCreatedBySalesPerson(originalSalesPerson);
        }

        // Assigned To = Initial Sales Person
        if (client.getSalesPerson() == null) {
            client.setSalesPerson(originalSalesPerson);
        }

        if (client.getClientSince() == null) {
            client.setClientSince(LocalDate.now());
        }
        client.setNextFollowupDate(nextFollowupDate); // Initial ~3-Month Follow-Up schedule

        Client savedClient = clientRepository.save(client);
        crmVerification.setClient(savedClient);
        CrmVerification savedCrmVerif = crmVerificationRepository.save(crmVerification);

        // Update Onboarding status to VERIFIED
        onboarding.setOnboardingStatus(CrmOnboardingStatus.VERIFIED);
        crmOnboardingRepository.save(onboarding);

        // Update Lead stage to CLIENT_ONBOARDED and status to CONVERTED
        lead.setLeadStatus(LeadStatus.CONVERTED);
        lead.setLeadStage(LeadStage.CLIENT_ONBOARDED);
        leadRepository.save(lead);

        log.info("CRM verification completed for lead: {}. Client created: {}, CreatedBy: {}, AssignedTo: {}, next follow-up: {}",
                lead.getLeadCode(), savedClient.getClientCode(),
                savedClient.getCreatedBySalesPerson() != null ? savedClient.getCreatedBySalesPerson().getEmployeeCode() : "null",
                savedClient.getSalesPerson() != null ? savedClient.getSalesPerson().getEmployeeCode() : "null",
                nextFollowupDate);

        return CrmVerificationResponse.builder()
                .id(savedCrmVerif.getId())
                .leadId(lead.getId())
                .leadCode(lead.getLeadCode())
                .clientId(savedClient.getId())
                .clientCode(savedClient.getClientCode())
                .clientName(savedClient.getClientName())
                .verificationStatus(savedCrmVerif.getVerificationStatus().name())
                .kycVerified(savedCrmVerif.getKycVerified())
                .bankDetailsVerified(savedCrmVerif.getBankDetailsVerified())
                .documentsVerified(savedCrmVerif.getDocumentsVerified())
                .clientContactConfirmed(savedCrmVerif.getClientContactConfirmed())
                .panNumber(savedCrmVerif.getPanNumber())
                .remarks(savedCrmVerif.getRemarks())
                .verifiedBy(savedCrmVerif.getVerifiedBy())
                .verifiedAt(savedCrmVerif.getVerifiedAt())
                .salesPersonCode(savedClient.getSalesPerson() != null ? savedClient.getSalesPerson().getEmployeeCode() : null)
                .salesPersonName(savedClient.getSalesPerson() != null ? savedClient.getSalesPerson().getFullName() : null)
                .createdBySalesPersonCode(savedClient.getCreatedBySalesPerson() != null ? savedClient.getCreatedBySalesPerson().getEmployeeCode() : null)
                .createdBySalesPersonName(savedClient.getCreatedBySalesPerson() != null ? savedClient.getCreatedBySalesPerson().getFullName() : null)
                .nextFollowupDate(savedClient.getNextFollowupDate())
                .build();
    }

    @Override
    public ClientResponse assignClient(String clientCode, CrmClientAssignRequest request, String currentUserEmail) {
        log.info("Assigning client: {} by CRM user: {}", clientCode, currentUserEmail);

        Client client = findClientByCodeOrThrow(clientCode);

        // Resolve target Sales Person
        User targetSalesPerson = null;
        if (request.getSalesPersonId() != null) {
            targetSalesPerson = userRepository.findById(request.getSalesPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getSalesPersonId()));
        } else if (request.getSalesPersonCode() != null && !request.getSalesPersonCode().isBlank()) {
            targetSalesPerson = userRepository.findByEmployeeCodeIgnoreCase(request.getSalesPersonCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with employee code: " + request.getSalesPersonCode()));
        } else if (request.getSalesPersonIdentifier() != null && !request.getSalesPersonIdentifier().isBlank()) {
            String identifier = request.getSalesPersonIdentifier().trim();
            targetSalesPerson = userRepository.findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
                    identifier, identifier, identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier));
        }

        if (targetSalesPerson == null) {
            throw new IllegalArgumentException("Target Sales Person must be specified for assignment.");
        }

        // CRITICAL BUSINESS RULE:
        // Update Assigned To, but NEVER overwrite Created By (Original Sales Person).
        client.setSalesPerson(targetSalesPerson);
        Client saved = clientRepository.save(client);

        log.info("Client {} reassigned to: {}. Original CreatedBy: {} remains intact.",
                client.getClientCode(),
                targetSalesPerson.getEmployeeCode(),
                saved.getCreatedBySalesPerson() != null ? saved.getCreatedBySalesPerson().getEmployeeCode() : "null");

        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientFollowUpResponse> getClientFollowUps(String salesPersonIdentifier, String currentUserEmail) {
        String identifier = salesPersonIdentifier != null && !salesPersonIdentifier.isBlank()
                ? salesPersonIdentifier.trim()
                : currentUserEmail;

        log.info("Fetching client follow-ups for sales person identifier: {}", identifier);

        Optional<User> userOpt = userRepository.findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
                identifier, identifier, identifier);

        List<Client> clients;
        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getId();
            clients = clientRepository.findAll((root, query, cb) -> cb.or(
                    cb.equal(root.get("salesPerson").get("id"), userId),
                    cb.equal(root.get("lead").get("assignedSalesPerson").get("id"), userId)
            ));
        } else {
            clients = clientRepository.findAll();
        }

        List<ClientFollowUpResponse> responses = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Client client : clients) {
            User sp = client.getSalesPerson() != null ? client.getSalesPerson()
                    : (client.getLead() != null ? client.getLead().getAssignedSalesPerson() : null);

            User createdBy = client.getCreatedBySalesPerson() != null ? client.getCreatedBySalesPerson()
                    : (client.getLead() != null ? client.getLead().getAssignedSalesPerson() : null);

            Long daysUntil = null;
            if (client.getNextFollowupDate() != null) {
                daysUntil = ChronoUnit.DAYS.between(today, client.getNextFollowupDate());
            }

            responses.add(ClientFollowUpResponse.builder()
                    .clientId(client.getId())
                    .clientCode(client.getClientCode())
                    .clientName(client.getClientName())
                    .mobileNumber(client.getMobileNumber())
                    .email(client.getEmail())
                    .salesPersonId(sp != null ? sp.getId() : null)
                    .salesPersonCode(sp != null ? sp.getEmployeeCode() : null)
                    .salesPersonName(sp != null ? sp.getFullName() : null)
                    .createdBySalesPersonId(createdBy != null ? createdBy.getId() : null)
                    .createdBySalesPersonCode(createdBy != null ? createdBy.getEmployeeCode() : null)
                    .createdBySalesPersonName(createdBy != null ? createdBy.getFullName() : null)
                    .clientSince(client.getClientSince())
                    .nextFollowupDate(client.getNextFollowupDate())
                    .daysUntilFollowUp(daysUntil)
                    .build());
        }

        return responses;
    }

    @Override
    public ClientFollowUpRecordResponse recordClientFollowUp(String clientCode, RecordClientFollowUpRequest request, String currentUserEmail) {
        log.info("Recording client follow-up for client: {}, by: {}", clientCode, currentUserEmail);

        Client client = findClientByCodeOrThrow(clientCode);

        // Sales Person handling the follow-up
        User salesPerson = client.getSalesPerson();
        if (salesPerson == null) {
            salesPerson = userRepository.findByEmailIgnoreCaseAndDeletedFalse(currentUserEmail)
                    .orElse(client.getCreatedBySalesPerson());
        }

        LocalDate followupDate = request.getFollowupDate() != null ? request.getFollowupDate() : LocalDate.now();

        // CLIENT FOLLOW-UP RULE: Recurring ~3 months
        LocalDate nextFollowupDate = request.getNextFollowupDate() != null
                ? request.getNextFollowupDate()
                : followupDate.plusMonths(3);

        ClientFollowUp followUp = ClientFollowUp.builder()
                .client(client)
                .salesPerson(salesPerson)
                .followupDate(followupDate)
                .remarks(request.getRemarks())
                .nextFollowupDate(nextFollowupDate)
                .build();

        ClientFollowUp savedFollowUp = clientFollowUpRepository.save(followUp);

        // Advance client's next follow-up schedule
        client.setNextFollowupDate(nextFollowupDate);
        clientRepository.save(client);

        log.info("Client follow-up recorded for client {}. Next follow-up scheduled for: {}",
                client.getClientCode(), nextFollowupDate);

        return ClientFollowUpRecordResponse.builder()
                .id(savedFollowUp.getId())
                .clientId(client.getId())
                .clientCode(client.getClientCode())
                .clientName(client.getClientName())
                .salesPersonId(salesPerson != null ? salesPerson.getId() : null)
                .salesPersonCode(salesPerson != null ? salesPerson.getEmployeeCode() : null)
                .salesPersonName(salesPerson != null ? salesPerson.getFullName() : null)
                .followupDate(savedFollowUp.getFollowupDate())
                .remarks(savedFollowUp.getRemarks())
                .nextFollowupDate(savedFollowUp.getNextFollowupDate())
                .createdAt(savedFollowUp.getCreatedAt())
                .createdBy(savedFollowUp.getCreatedBy())
                .build();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Lead findLeadByCodeOrThrow(String leadCode) {
        return leadRepository.findByLeadCode(leadCode)
                .or(() -> leadRepository.findByUniqueLeadId(leadCode))
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with code: " + leadCode));
    }

    private Client findClientByCodeOrThrow(String clientCode) {
        Optional<Client> clientOpt = clientRepository.findByClientCode(clientCode);
        if (clientOpt.isPresent()) {
            return clientOpt.get();
        }
        try {
            Long id = Long.parseLong(clientCode);
            return clientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with code or ID: " + clientCode));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("Client not found with code: " + clientCode);
        }
    }

    private void validateLeadEligibleForCrm(Lead lead) {
        // 1. Enforce business rule: WIP MUST NOT go to CRM
        if (lead.getLeadStatus() == LeadStatus.WORK_IN_PROGRESS) {
            throw new IllegalStateException("Work In Progress leads cannot be processed by CRM.");
        }

        // 2. Enforce business rule: Lead must only reach CRM AFTER PC Meeting Verification
        Optional<MeetingVerification> pcVerificationOpt = meetingVerificationRepository
                .findTopByMeetingLeadIdAndVerificationStatusOrderByMeetingMeetingNumberDesc(
                        lead.getId(), VerificationStatus.VERIFIED);

        if (pcVerificationOpt.isEmpty()) {
            throw new IllegalStateException("Cannot process CRM onboarding: Lead has not been verified by Process Coordinator.");
        }

        MeetingVerification pcVerification = pcVerificationOpt.get();
        if (pcVerification.getMeeting().getLeadStatus() != MeetingLeadStatus.CONVERTED_CLIENT) {
            throw new IllegalStateException("Meeting verification outcome was not CONVERTED_CLIENT. Lead cannot enter CRM.");
        }
    }

    private User resolveOriginalSalesPerson(Lead lead) {
        if (lead.getAssignedSalesPerson() != null) {
            return lead.getAssignedSalesPerson();
        }
        Optional<MeetingVerification> pcVerificationOpt = meetingVerificationRepository
                .findTopByMeetingLeadIdAndVerificationStatusOrderByMeetingMeetingNumberDesc(
                        lead.getId(), VerificationStatus.VERIFIED);
        if (pcVerificationOpt.isPresent() && pcVerificationOpt.get().getMeeting().getAssignedEmployee() != null) {
            return pcVerificationOpt.get().getMeeting().getAssignedEmployee();
        }
        return null;
    }

    private CrmOnboardingResponse mapToOnboardingResponse(CrmOnboarding entity) {
        User sp = entity.getCreatedBySalesPerson();
        return CrmOnboardingResponse.builder()
                .id(entity.getId())
                .leadId(entity.getLead().getId())
                .leadCode(entity.getLead().getLeadCode())
                .onboardingStatus(entity.getOnboardingStatus() != null ? entity.getOnboardingStatus().name() : null)
                .createdBySalesPersonId(sp != null ? sp.getId() : null)
                .createdBySalesPersonCode(sp != null ? sp.getEmployeeCode() : null)
                .createdBySalesPersonName(sp != null ? sp.getFullName() : null)
                .investorName(entity.getInvestorName())
                .isBlueantInvestor(entity.getIsBlueantInvestor())
                .familyHead(entity.getFamilyHead())
                .occupation(entity.getOccupation())
                .panNumber(entity.getPanNumber())
                .contactDetail(entity.getContactDetail())
                .mailId(entity.getMailId())
                .correspondenceAddress(entity.getCorrespondenceAddress())
                .officeAddress(entity.getOfficeAddress())
                .placeOfBirth(entity.getPlaceOfBirth())
                .familyDetails(entity.getFamilyDetails())
                .location(entity.getLocation())
                .source(entity.getSource())
                .sourceDescription(entity.getSourceDescription())
                .applicationReceivedDate(entity.getApplicationReceivedDate())
                .nomineeDetails(entity.getNomineeDetails())
                .nomineePanOrAadhaar(entity.getNomineePanOrAadhaar())
                .motherName(entity.getMotherName())
                .applicationMode(entity.getApplicationMode())
                .firstInvestmentAmount(entity.getFirstInvestmentAmount())
                .expectedMaxSIP(entity.getExpectedMaxSIP())
                .investmentType(entity.getInvestmentType())
                .allDocumentsCompleted(entity.getAllDocumentsCompleted())
                .investwellUserId(entity.getInvestwellUserId())
                .helpdeskQueryNo(entity.getHelpdeskQueryNo())
                .clientReportedDate(entity.getClientReportedDate())
                .paymentDone(entity.getPaymentDone())
                .remarks(entity.getRemarks())
                .submittedAt(entity.getSubmittedAt())
                .submittedBy(entity.getSubmittedBy())
                .build();
    }
}
