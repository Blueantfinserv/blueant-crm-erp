package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.*;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final com.blueant_crm_erp.lead.repository.LeadRepository leadRepository;
    private final com.blueant_crm_erp.meeting.service.FollowUpService followUpService;
    private final com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository meetingUpdateRepository;

    @Override
    public MeetingResponse verifyMeeting(String meetingCode, MeetingVerificationRequest request, String currentUserEmail) {
        log.info("Verifying meeting: {} by Sales Coordinator: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = findMeetingByCode(meetingCode);

        // Validate coordinator has permission
        boolean hasPermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("MEETING_VERIFY") ||
                        auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!hasPermission) {
            throw new AccessDeniedException("User does not have verification permission.");
        }

        if (meeting.getMeetingStatus() != MeetingStatus.COMPLETED &&
            meeting.getMeetingStatus() != MeetingStatus.NOT_CONDUCTED) {
            throw new IllegalArgumentException("Meeting must be completed.");
        }

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(meeting.getId())
                .orElseThrow(() -> new IllegalArgumentException("No verification record found for this meeting."));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new IllegalArgumentException("Meeting verification status must be PENDING.");
        }

        // Validate coordinator questions
        validateCoordinatorData(meeting, request);

        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedBy(currentUserEmail);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setRejectionReason(null);

        // Map and normalize coordinator answers
        verification.setMeetingTiming(request.getMeetingTiming());
        verification.setAgeGroup(parseEnum(AgeGroup.class, request.getAgeGroup(), "ageGroup"));
        verification.setExistingSip(parseEnum(ExistingSip.class, request.getExistingSip(), "existingSip"));
        verification.setProfessionDetail(normalizeString(request.getProfessionDetail(), 255, "professionDetail"));
        verification.setBestTimeForMeeting(parseEnum(BestTimeForMeeting.class, request.getBestTimeForMeeting(), "bestTimeForMeeting"));

        Profession parsedProfession = parseEnum(Profession.class, request.getProfession(), "profession");
        verification.setProfession(parsedProfession != null ? parsedProfession.name() : null);

        // Map meetingWith / aloneWith logic
        String aloneWithValue = null;
        MeetingWith meetingWithEnum = parseEnum(MeetingWith.class, request.getMeetingWith(), "meetingWith");
        if (meetingWithEnum != null) {
            if (meetingWithEnum == MeetingWith.SELF) {
                aloneWithValue = "SELF";
            } else if (meetingWithEnum == MeetingWith.SOMEONE_ELSE) {
                aloneWithValue = "SOMEONE";
            }
        } else {
            // Fallback to legacy aloneWith
            String legacyAloneWith = normalizeString(request.getAloneWith(), 20, "aloneWith");
            if (legacyAloneWith != null) {
                if ("SELF".equalsIgnoreCase(legacyAloneWith)) {
                    aloneWithValue = "SELF";
                } else if ("SOMEONE".equalsIgnoreCase(legacyAloneWith)) {
                    aloneWithValue = "SOMEONE";
                } else {
                    throw new IllegalArgumentException("aloneWith must be SELF or SOMEONE");
                }
            }
        }

        String verifiedPersonName = null;
        String verifiedPosition = null;
        if ("SOMEONE".equals(aloneWithValue)) {
            verifiedPersonName = normalizeString(request.getPersonName(), 100, "personName");
            verifiedPosition = normalizeString(request.getPosition(), 100, "position");
        }

        verification.setAloneWith(aloneWithValue);
        verification.setPersonName(verifiedPersonName);
        verification.setPosition(verifiedPosition);

        // Map rest of optional/legacy fields
        verification.setClientAge(request.getClientAge());
        verification.setMaritalStatus(normalizeString(request.getMaritalStatus(), 50, "maritalStatus"));
        verification.setEmail(normalizeString(request.getEmail(), 150, "email"));
        verification.setCompanyName(normalizeString(request.getCompanyName(), 150, "companyName"));
        verification.setAnyChildren(request.getAnyChildren());
        if (Boolean.TRUE.equals(request.getAnyChildren())) {
            verification.setNumberOfChildren(request.getNumberOfChildren());
        } else {
            verification.setNumberOfChildren(0);
        }
        verification.setPreviousInvestment(request.getPreviousInvestment());

        // --- Populate Complete Verified Meeting Snapshot ---
        // 1. Meeting Identity
        verification.setMeetingCode(meeting.getMeetingCode());
        verification.setMeetingNumber(meeting.getMeetingNumber());
        verification.setMeetingType(meeting.getMeetingType());
        verification.setMeetingTitle(meeting.getMeetingTitle());

        // 2. Lead / Client Snapshot
        com.blueant_crm_erp.lead.entity.Lead lead = meeting.getLead();
        if (lead != null) {
            verification.setLeadId(lead.getId());
            verification.setLeadCode(lead.getLeadCode());
            verification.setClientName(lead.getClientName());
            verification.setMobileNumber(lead.getMobileNumber());
        }

        // 3. Sales Person Snapshot
        com.blueant_crm_erp.user.entity.User assignedEmployee = meeting.getAssignedEmployee();
        if (assignedEmployee == null && lead != null) {
            assignedEmployee = lead.getAssignedSalesPerson();
        }
        if (assignedEmployee != null) {
            verification.setAssignedEmployeeId(assignedEmployee.getId());
            verification.setEmployeeCode(assignedEmployee.getEmployeeCode());
            verification.setEmployeeName(assignedEmployee.getFullName());
        }

        // 4. Meeting Execution Snapshot
        java.time.LocalDate verifiedMeetingDate = request.getMeetingDate() != null ? request.getMeetingDate() : meeting.getMeetingDate();
        verification.setMeetingDate(verifiedMeetingDate);
        verification.setMeetingTime(meeting.getMeetingTime());
        verification.setMeetingMode(meeting.getMeetingMode());
        String location = meeting.getMeetingLocation();
        if ((location == null || location.isBlank()) && lead != null) {
            location = lead.getLocation();
        }
        verification.setMeetingLocation(location);
        verification.setMeetingStatus(meeting.getMeetingStatus());
        verification.setStatus(meeting.getStatus());
        verification.setMeetingRemarks(meeting.getMeetingRemarks());
        verification.setNextMeetingDate(meeting.getNextMeetingDate());
        verification.setNextMeetingTime(meeting.getNextMeetingTime());
        verification.setMeetingConducted(meeting.getMeetingConducted());
        verification.setLeadStatus(meeting.getLeadStatus());

        // 5. Captured Meeting GPS & Visiting Card Data
        verification.setLatitude(meeting.getLatitude());
        verification.setLongitude(meeting.getLongitude());
        verification.setLocationAccuracy(meeting.getLocationAccuracy());
        verification.setLocationCapturedAt(meeting.getLocationCapturedAt());
        verification.setGoogleMapsUrl(meeting.getGoogleMapsUrl());
        verification.setVisitingCard(meeting.getVisitingCard());

        meetingVerificationRepository.save(verification);

        // Keep existing meeting entity fields in sync
        meeting.setMeetingDate(verifiedMeetingDate);
        meeting.setVerifiedByProcessCoordinator(true);
        meeting.setMeetingVerificationDate(LocalDateTime.now());
        meeting.setVerificationRemarks(request.getRemarks());
        meeting.setVerifiedBy(currentUserEmail);
        meeting.setVerification(verification);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        handlePostVerificationDecision(savedMeeting, currentUserEmail);
        return meetingMapper.toResponse(savedMeeting);
    }

    private void handlePostVerificationDecision(Meeting meeting, String currentUserEmail) {
        if (meeting.getLead() == null) {
            return;
        }

        com.blueant_crm_erp.lead.entity.Lead lead = meeting.getLead();

        if (meeting.getMeetingConducted() == com.blueant_crm_erp.meeting.enums.MeetingConductStatus.NOT_CONDUCTED) {
            lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.WORK_IN_PROGRESS);
            lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.FOLLOW_UP);
            leadRepository.save(lead);
            log.info("[PostPcVerification] Lead {} confirmed WORK_IN_PROGRESS (Stage: FOLLOW_UP) after NOT_CONDUCTED visit verification.",
                    lead.getLeadCode());

            // After successful PC verification of a NOT_CONDUCTED meeting with a valid nextPlanDate,
            // create exactly ONE NEW scheduled meeting for that nextPlanDate.
            java.time.LocalDate nextPlanDate = meeting.getNextMeetingDate();
            java.time.LocalTime nextPlanTime = meeting.getNextMeetingTime();
            if (nextPlanDate == null) {
                java.util.Optional<com.blueant_crm_erp.meeting.entity.MeetingUpdate> latestUpdate =
                        meetingUpdateRepository.findTopByMeetingIdOrderByUpdateNumberDesc(meeting.getId());
                if (latestUpdate.isPresent() && latestUpdate.get().getNextPlanDate() != null) {
                    nextPlanDate = latestUpdate.get().getNextPlanDate();
                    nextPlanTime = latestUpdate.get().getNextPlanTime();
                }
            }

            if (nextPlanDate != null) {
                boolean alreadyHasScheduled = meetingRepository.existsByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.SCHEDULED);
                int nextSequence = meeting.getMeetingNumber() + 1;
                boolean nextSequenceExists = meetingRepository.existsByLeadIdAndMeetingNumber(lead.getId(), nextSequence);

                if (!alreadyHasScheduled && !nextSequenceExists) {
                    Meeting nextMeeting = followUpService.createFollowUp(
                            meeting, nextPlanDate, nextPlanTime, meeting.getMeetingRemarks(), currentUserEmail);
                    eventPublisher.publishEvent(new com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent(
                            this, meeting, nextMeeting, currentUserEmail));
                    log.info("[PostPcVerification] Auto-scheduled next meeting {} (seq #{}, type: {}) for lead {} on {}",
                            nextMeeting.getMeetingCode(), nextMeeting.getMeetingNumber(), nextMeeting.getMeetingType(),
                            lead.getLeadCode(), nextPlanDate);
                } else {
                    log.info("[PostPcVerification] Skipping duplicate meeting creation for lead {}. (alreadyHasScheduled={}, nextSequenceExists={})",
                            lead.getLeadCode(), alreadyHasScheduled, nextSequenceExists);
                }
            }
            return;
        }

        MeetingLeadStatus outcome = meeting.getLeadStatus();
        if (outcome == null) {
            return;
        }

        switch (outcome) {
            case WORK_IN_PROGRESS -> {
                // CASE A — LEAD STATUS = WORK_IN_PROGRESS
                // Continuous cycle: remains WIP, stage FOLLOW_UP. WIP MUST NOT go to CRM.
                lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.WORK_IN_PROGRESS);
                lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.FOLLOW_UP);
                leadRepository.save(lead);
                log.info("[PostPcVerification] Lead {} confirmed WORK_IN_PROGRESS (Stage: FOLLOW_UP) after PC verification.",
                        lead.getLeadCode());
            }
            case CONVERTED_CLIENT -> {
                // CASE B — LEAD STATUS = CONVERTED_CLIENT
                // Transition to CRM_HANDOVER / CRM entry queue. Eligible for CRM ONLY AFTER PC verification.
                lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.CONVERTED);
                lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.CRM_HANDOVER);
                leadRepository.save(lead);
                log.info("[PostPcVerification] Lead {} transitioned to CONVERTED / CRM_HANDOVER after PC verification. Now eligible for CRM.",
                        lead.getLeadCode());
            }
            case ALREADY_CLIENT -> {
                // CASE C — OTHER STATUS: Excluded from active work; records kept intact.
                lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.ALREADY_CLIENT);
                lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.COMPLETED);
                leadRepository.save(lead);
                log.info("[PostPcVerification] Lead {} marked ALREADY_CLIENT. Excluded from Sales Person active work.",
                        lead.getLeadCode());
            }
            case CLIENT_REMOVED -> {
                // CASE C — OTHER STATUS: Excluded from active work; records kept intact.
                lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.REMOVED);
                lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.COMPLETED);
                leadRepository.save(lead);
                log.info("[PostPcVerification] Lead {} marked REMOVED. Excluded from Sales Person active work.",
                        lead.getLeadCode());
            }
            case CLIENT_NOT_INTERESTED -> {
                // CASE C — OTHER STATUS: Excluded from active work; records kept intact.
                lead.setLeadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.NOT_INTERESTED);
                lead.setLeadStage(com.blueant_crm_erp.lead.enums.LeadStage.COMPLETED);
                leadRepository.save(lead);
                log.info("[PostPcVerification] Lead {} marked NOT_INTERESTED. Excluded from Sales Person active work.",
                        lead.getLeadCode());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public com.blueant_crm_erp.meeting.dto.response.MeetingVerificationResponse getVerification(String meetingCode) {
        log.info("Fetching verification record for meeting code: {}", meetingCode);
        Meeting meeting = findMeetingByCode(meetingCode);
        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(meeting.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No verification record found for meeting: " + meetingCode));
        return meetingMapper.toVerificationResponse(verification);
    }

    @Override
    public MeetingResponse rejectMeeting(String meetingCode, String reason, String currentUserEmail) {
        log.info("Rejecting meeting verification: {} by Sales Coordinator: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = findMeetingByCode(meetingCode);

        // Validate coordinator has permission
        boolean hasPermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("MEETING_VERIFY") ||
                        auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!hasPermission) {
            throw new AccessDeniedException("User does not have verification permission.");
        }

        if (meeting.getMeetingStatus() != MeetingStatus.COMPLETED &&
            meeting.getMeetingStatus() != MeetingStatus.NOT_CONDUCTED) {
            throw new IllegalArgumentException("Meeting must be completed.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(meeting.getId())
                .orElseThrow(() -> new IllegalArgumentException("No verification record found for this meeting."));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new IllegalArgumentException("Meeting verification status must be PENDING.");
        }

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

    private void validateCoordinatorData(Meeting meeting, MeetingVerificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Verification request cannot be null.");
        }
        if (meeting.getMeetingConducted() == com.blueant_crm_erp.meeting.enums.MeetingConductStatus.CONDUCTED) {
            if (request.getMeetingTiming() == null) {
                throw new IllegalArgumentException("Meeting timing is required.");
            }
        }
        if (Boolean.TRUE.equals(request.getAnyChildren())) {
            if (request.getNumberOfChildren() == null || request.getNumberOfChildren() <= 0) {
                throw new IllegalArgumentException("Number of children must be greater than 0 if anyChildren is true");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (enumClass == AgeGroup.class && "AGE_35_45".equalsIgnoreCase(trimmed)) {
            return (E) AgeGroup.AGE_36_45;
        }
        try {
            return Enum.valueOf(enumClass, trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for " + fieldName + ": " + trimmed);
        }
    }

    private String normalizeString(String input, int maxLength, String fieldName) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters.");
        }
        return trimmed;
    }

    private Meeting findMeetingByCode(String meetingCode) {
        if (meetingCode == null || meetingCode.isBlank()) {
            throw new ResourceNotFoundException("Meeting code is required.");
        }
        String normalizedCode = meetingCode.trim();
        return meetingRepository.findByMeetingCode(normalizedCode)
                .or(() -> meetingRepository.findByMeetingCodeIgnoreCase(normalizedCode))
                .or(() -> meetingRepository.findByMeetingCodeNormalized(normalizedCode))
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with code: " + meetingCode));
    }
}
