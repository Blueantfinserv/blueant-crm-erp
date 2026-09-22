package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientFollowUpResponse;
import com.blueant_crm_erp.client.dto.response.CrmLeadQueueResponse;
import com.blueant_crm_erp.client.dto.response.CrmVerificationResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.CrmVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientRepository;
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
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CrmLeadQueueResponse> getCrmQueue(String currentUserEmail) {
        log.info("Fetching CRM queue for user: {}", currentUserEmail);

        // Fetch leads that are potential conversion candidates:
        // Exclude WIP, REMOVED, NOT_INTERESTED, etc.
        List<Lead> candidateLeads = new ArrayList<>();
        candidateLeads.addAll(leadRepository.findAllByLeadStatus(LeadStatus.CONVERTED));

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
                    .crmVerificationStatus(VerificationStatus.PENDING.name())
                    .build();

            queue.add(response);
        }

        return queue;
    }

    @Override
    public CrmVerificationResponse verifyCrm(String leadCode, CrmVerificationRequest request, String currentUserEmail) {
        log.info("Processing CRM verification for lead: {}, by: {}", leadCode, currentUserEmail);

        Lead lead = leadRepository.findByLeadCode(leadCode)
                .or(() -> leadRepository.findByUniqueLeadId(leadCode))
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with code: " + leadCode));

        // 1. Enforce business rule: WIP MUST NOT go to CRM
        if (lead.getLeadStatus() == LeadStatus.WORK_IN_PROGRESS) {
            throw new IllegalStateException("Work In Progress leads cannot be processed by CRM.");
        }

        // 2. Enforce business rule: Lead must only reach CRM AFTER PC Meeting Verification
        Optional<MeetingVerification> pcVerificationOpt = meetingVerificationRepository
                .findTopByMeetingLeadIdAndVerificationStatusOrderByMeetingMeetingNumberDesc(
                        lead.getId(), VerificationStatus.VERIFIED);

        if (pcVerificationOpt.isEmpty()) {
            throw new IllegalStateException("Cannot perform CRM verification: Lead has not been verified by Process Coordinator.");
        }

        MeetingVerification pcVerification = pcVerificationOpt.get();
        if (pcVerification.getMeeting().getLeadStatus() != MeetingLeadStatus.CONVERTED_CLIENT) {
            throw new IllegalStateException("Meeting verification outcome was not CONVERTED_CLIENT. Lead cannot be verified in CRM.");
        }

        // 3. Populate / Save CRM Verification Record
        CrmVerification crmVerification = crmVerificationRepository.findByLeadId(lead.getId())
                .orElseGet(() -> CrmVerification.builder().lead(lead).build());

        crmVerification.setVerificationStatus(VerificationStatus.VERIFIED);
        crmVerification.setKycVerified(Boolean.TRUE.equals(request.getKycVerified()));
        crmVerification.setBankDetailsVerified(Boolean.TRUE.equals(request.getBankDetailsVerified()));
        crmVerification.setDocumentsVerified(Boolean.TRUE.equals(request.getDocumentsVerified()));
        crmVerification.setClientContactConfirmed(Boolean.TRUE.equals(request.getClientContactConfirmed()));
        crmVerification.setPanNumber(request.getPanNumber() != null ? request.getPanNumber().trim() : null);
        crmVerification.setRemarks(request.getRemarks());
        crmVerification.setVerifiedBy(currentUserEmail);
        crmVerification.setVerifiedAt(LocalDateTime.now());
        crmVerification.setRejectionReason(null);

        // 4. Create / Activate Client entity preserving original Sales Person
        User salesPerson = lead.getAssignedSalesPerson();
        if (salesPerson == null && pcVerification.getMeeting().getAssignedEmployee() != null) {
            salesPerson = pcVerification.getMeeting().getAssignedEmployee();
        }

        LocalDate nextFollowupDate = LocalDate.now().plusMonths(3);

        Client client = clientRepository.findByLeadId(lead.getId())
                .orElseGet(() -> Client.builder()
                        .lead(lead)
                        .clientCode("CLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .build());

        client.setClientStatus(ClientStatus.ACTIVE);
        client.setClientName(lead.getClientName());
        client.setMobileNumber(lead.getMobileNumber());
        client.setEmail(lead.getEmail());
        if (request.getPanNumber() != null && !request.getPanNumber().isBlank()) {
            client.setPanNumber(request.getPanNumber().trim());
        }
        client.setSalesPerson(salesPerson); // Preserve original Sales Person relationship
        if (client.getClientSince() == null) {
            client.setClientSince(LocalDate.now());
        }
        client.setNextFollowupDate(nextFollowupDate); // ~3-Month Follow-Up schedule

        Client savedClient = clientRepository.save(client);
        crmVerification.setClient(savedClient);
        CrmVerification savedCrmVerif = crmVerificationRepository.save(crmVerification);

        // 5. Update Lead stage to CLIENT_ONBOARDED and status to CONVERTED
        lead.setLeadStatus(LeadStatus.CONVERTED);
        lead.setLeadStage(LeadStage.CLIENT_ONBOARDED);
        leadRepository.save(lead);

        log.info("CRM verification completed for lead: {}. Client created: {}, next follow-up: {}",
                lead.getLeadCode(), savedClient.getClientCode(), nextFollowupDate);

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
                .salesPersonCode(salesPerson != null ? salesPerson.getEmployeeCode() : null)
                .salesPersonName(salesPerson != null ? salesPerson.getFullName() : null)
                .nextFollowupDate(savedClient.getNextFollowupDate())
                .build();
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
                    .clientSince(client.getClientSince())
                    .nextFollowupDate(client.getNextFollowupDate())
                    .daysUntilFollowUp(daysUntil)
                    .build());
        }

        return responses;
    }
}
