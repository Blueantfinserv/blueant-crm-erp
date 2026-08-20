package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessCoordinatorServiceImpl implements ProcessCoordinatorService {

    private final MeetingRepository meetingRepository;
    private final MeetingVerificationRepository meetingVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeetingMapper meetingMapper;

    @Override
    public MeetingResponse verifyMeeting(String meetingCode, MeetingVerificationRequest request, String currentUserEmail) {
        log.info("Verifying meeting: {} by Sales Coordinator: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid meeting code"));

        if (meeting.getMeetingStatus() == MeetingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled meetings cannot be verified.");
        }

        // Validate coordinator questions
        validateCoordinatorData(request);

        // Fetch or create verification entity
        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(meeting.getId())
                .orElseGet(() -> MeetingVerification.builder().meeting(meeting).build());

        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedBy(currentUserEmail);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setRejectionReason(null);

        // Map coordinator answers
        verification.setAloneWith(request.getAloneWith());
        if ("SOMEONE".equalsIgnoreCase(request.getAloneWith())) {
            verification.setPersonName(request.getPersonName());
            verification.setPosition(request.getPosition());
        } else {
            verification.setPersonName(null);
            verification.setPosition(null);
        }
        verification.setClientAge(request.getClientAge());
        verification.setMaritalStatus(request.getMaritalStatus());
        verification.setProfession(request.getProfession());
        verification.setEmail(request.getEmail());
        verification.setCompanyName(request.getCompanyName());
        verification.setAnyChildren(request.getAnyChildren());
        if (Boolean.TRUE.equals(request.getAnyChildren())) {
            verification.setNumberOfChildren(request.getNumberOfChildren());
        } else {
            verification.setNumberOfChildren(0);
        }
        verification.setPreviousInvestment(request.getPreviousInvestment());

        meetingVerificationRepository.save(verification);

        // Keep existing meeting entity fields in sync
        meeting.setVerifiedByProcessCoordinator(true);
        meeting.setMeetingVerificationDate(LocalDateTime.now());
        meeting.setVerificationRemarks(request.getRemarks());
        meeting.setVerifiedBy(currentUserEmail);
        meeting.setVerification(verification);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public MeetingResponse rejectMeeting(String meetingCode, String reason, String currentUserEmail) {
        log.info("Rejecting meeting verification: {} by Sales Coordinator: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid meeting code"));

        if (meeting.getMeetingStatus() == MeetingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled meetings cannot be rejected.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        // Fetch or create verification entity
        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(meeting.getId())
                .orElseGet(() -> MeetingVerification.builder().meeting(meeting).build());

        verification.setVerificationStatus(VerificationStatus.REJECTED);
        verification.setVerifiedBy(currentUserEmail);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setRejectionReason(reason);

        meetingVerificationRepository.save(verification);

        // Keep existing meeting entity fields in sync
        meeting.setVerifiedByProcessCoordinator(false);
        meeting.setMeetingVerificationDate(LocalDateTime.now());
        meeting.setVerificationRemarks("REJECTED: " + reason);
        meeting.setVerifiedBy(currentUserEmail);
        meeting.setVerification(verification);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    private void validateCoordinatorData(MeetingVerificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Verification request cannot be null.");
        }
        if ("SOMEONE".equalsIgnoreCase(request.getAloneWith())) {
            if (request.getPersonName() == null || request.getPersonName().isBlank()) {
                throw new IllegalArgumentException("Person name is required if aloneWith is SOMEONE");
            }
            if (request.getPosition() == null || request.getPosition().isBlank()) {
                throw new IllegalArgumentException("Position is required if aloneWith is SOMEONE");
            }
        }
        if (Boolean.TRUE.equals(request.getAnyChildren())) {
            if (request.getNumberOfChildren() == null || request.getNumberOfChildren() <= 0) {
                throw new IllegalArgumentException("Number of children must be greater than 0 if anyChildren is true");
            }
        }
    }
}
