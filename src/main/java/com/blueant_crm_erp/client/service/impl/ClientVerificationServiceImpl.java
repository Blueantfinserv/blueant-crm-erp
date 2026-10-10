package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingVerificationResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientVerificationService;
import com.blueant_crm_erp.client.validator.ClientValidator;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientVerificationServiceImpl implements ClientVerificationService {

    private final ClientMeetingRepository clientMeetingRepository;
    private final ClientMeetingVerificationRepository clientMeetingVerificationRepository;
    private final ClientRepository clientRepository;
    private final ClientValidator clientValidator;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientMeetingVerificationResponse> getPendingVerifications(Pageable pageable) {
        Page<ClientMeetingVerification> page = clientMeetingVerificationRepository
                .findByVerificationStatusAndIsCurrentTrue(VerificationStatus.PENDING, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientMeetingVerificationResponse> getVerificationHistory(String meetingCode) {
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        return clientMeetingVerificationRepository.findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientMeetingVerificationResponse verifyMeeting(
            String meetingCode, ClientMeetingVerificationRequest request, String currentUserIdentifier) {

        log.info("PC verifying client meeting: {}, by: {}", meetingCode, currentUserIdentifier);

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId())
                .orElseThrow(() -> new BadRequestException("No active verification found for meeting: " + meetingCode));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Meeting verification is not in PENDING state. Current status: " + verification.getVerificationStatus());
        }

        // Validate conditional PC questions based on Conducted vs Not Conducted & Business Generated YES/NO
        clientValidator.validateVerification(meeting, request);

        boolean isConducted = meeting.getMeetingConducted() == MeetingConductStatus.CONDUCTED;

        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedBy(currentUserIdentifier);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setRejectionReason(null);

        if (isConducted) {
            verification.setNewMeetingDate(request.getNewMeetingDate());
            verification.setMeetingTime(request.getMeetingTime());
            verification.setBlueantAppUsed(request.getBlueantAppUsed() != null ? request.getBlueantAppUsed().trim().toUpperCase() : null);
            verification.setAloneWith(request.getAloneWith() != null ? request.getAloneWith().trim().toUpperCase() : null);
            verification.setPersonName(request.getPersonName());
            verification.setPosition(request.getPosition() != null ? request.getPosition().trim().toUpperCase() : null);
            verification.setRemarks(request.getRemarks());
            verification.setNextFollowupDate(request.getNextFollowupDate());

            boolean bgYes = "YES".equalsIgnoreCase(meeting.getBusinessGenerated()) || "YES".equalsIgnoreCase(request.getBusinessGenerated());
            if (bgYes) {
                verification.setBusinessGenerated("YES");
                verification.setInvestmentType(request.getInvestmentType());
                verification.setInvestmentAmount(request.getInvestmentAmount());
            } else {
                verification.setBusinessGenerated("NO");
                verification.setInvestmentType(null);
                verification.setInvestmentAmount(null);
            }
        } else {
            LocalDate notConductedDate = request.getMeetingDate() != null ? request.getMeetingDate() : request.getNewMeetingDate();
            verification.setNewMeetingDate(notConductedDate);
            verification.setMeetingTime(request.getMeetingTime());
            verification.setRemarks(request.getRemarks());
            verification.setNextFollowupDate(request.getNextFollowupDate());
            verification.setBlueantAppUsed(null);
            verification.setAloneWith(null);
            verification.setPersonName(null);
            verification.setPosition(null);
            verification.setBusinessGenerated(null);
            verification.setInvestmentType(null);
            verification.setInvestmentAmount(null);
        }

        ClientMeetingVerification savedVerif = clientMeetingVerificationRepository.save(verification);

        // Update meeting state
        meeting.setVerifiedByPc(true);
        meeting.setMeetingStatus(isConducted ? MeetingStatus.COMPLETED : MeetingStatus.NOT_CONDUCTED);
        clientMeetingRepository.save(meeting);

        // Update client next follow-up and investment type if verified
        Client client = meeting.getClient();
        if (client != null) {
            if (request.getNextFollowupDate() != null) {
                client.setNextFollowupDate(request.getNextFollowupDate());
            }
            if (StringUtils.hasText(savedVerif.getInvestmentType())) {
                client.setInvestmentType(savedVerif.getInvestmentType());
            }
            clientRepository.save(client);
        }

        log.info("Client meeting {} successfully verified by PC: {}. Attempt #{}",
                meetingCode, currentUserIdentifier, savedVerif.getAttemptNumber());

        return mapToResponse(savedVerif);
    }

    @Override
    @Transactional
    public ClientMeetingVerificationResponse rejectMeeting(
            String meetingCode, String reason, String currentUserIdentifier) {

        log.info("PC rejecting client meeting: {}, reason: {}, by: {}", meetingCode, reason, currentUserIdentifier);

        if (!StringUtils.hasText(reason)) {
            throw new BadRequestException("Rejection reason is required.");
        }

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Client meeting not found: " + meetingCode));

        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId())
                .orElseThrow(() -> new BadRequestException("No active verification found for meeting: " + meetingCode));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Meeting verification is not in PENDING state. Current status: " + verification.getVerificationStatus());
        }

        verification.setVerificationStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(reason.trim());
        verification.setVerifiedBy(currentUserIdentifier);
        verification.setVerifiedAt(LocalDateTime.now());

        ClientMeetingVerification savedVerif = clientMeetingVerificationRepository.save(verification);

        // Mark meeting verifiedByPc as false so RM/SC can correct and resubmit
        meeting.setVerifiedByPc(false);
        clientMeetingRepository.save(meeting);

        log.info("Client meeting {} rejected by PC: {}. Attempt #{}. Historical record preserved.",
                meetingCode, currentUserIdentifier, savedVerif.getAttemptNumber());

        return mapToResponse(savedVerif);
    }

    private ClientMeetingVerificationResponse mapToResponse(ClientMeetingVerification v) {
        ClientMeeting m = v.getClientMeeting();
        return ClientMeetingVerificationResponse.builder()
                .id(v.getId())
                .clientMeetingId(m != null ? m.getId() : null)
                .meetingCode(m != null ? m.getMeetingCode() : null)
                .attemptNumber(v.getAttemptNumber())
                .isCurrent(v.getIsCurrent())
                .verificationStatus(v.getVerificationStatus() != null ? v.getVerificationStatus().name() : null)
                .verifiedBy(v.getVerifiedBy())
                .verifiedAt(v.getVerifiedAt())
                .rejectionReason(v.getRejectionReason())
                .newMeetingDate(v.getNewMeetingDate())
                .meetingTime(v.getMeetingTime())
                .blueantAppUsed(v.getBlueantAppUsed())
                .aloneWith(v.getAloneWith())
                .personName(v.getPersonName())
                .position(v.getPosition())
                .businessGenerated(v.getBusinessGenerated())
                .investmentType(v.getInvestmentType())
                .investmentAmount(v.getInvestmentAmount())
                .remarks(v.getRemarks())
                .nextFollowupDate(v.getNextFollowupDate())
                .createdAt(v.getCreatedAt())
                .createdBy(v.getCreatedBy())
                .build();
    }
}
