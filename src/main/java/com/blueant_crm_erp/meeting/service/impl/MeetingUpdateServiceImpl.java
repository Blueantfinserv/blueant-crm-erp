package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingUpdateResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.mapper.MeetingUpdateMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository;
import com.blueant_crm_erp.meeting.service.MeetingUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 * Meeting Update Service Implementation (Redesigned & Cleaned)
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingUpdateServiceImpl implements MeetingUpdateService {

    private final MeetingUpdateRepository meetingUpdateRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingUpdateMapper meetingUpdateMapper;

    @Override
    public MeetingUpdate persistUpdate(Meeting meeting, MeetingWorkflowRequest request, String submittedBy) {
        log.info("Persisting meeting update for meeting: {}, submitted by: {}", meeting.getMeetingCode(), submittedBy);

        // Calculate next sequential update number
        long existingUpdates = meetingUpdateRepository.countByMeetingId(meeting.getId());
        int nextUpdateNumber = (int) existingUpdates + 1;

        // Generate backend GPS metadata - Server timestamp. Never trust frontend timestamps.
        LocalDateTime capturedAt = null;
        String googleMapsUrl = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            capturedAt = LocalDateTime.now();
            googleMapsUrl = "https://www.google.com/maps?q=" + request.getLatitude() + "," + request.getLongitude();
        }

        MeetingConductStatus conductStatus = request.getMeetingConducted() != null ? request.getMeetingConducted() : MeetingConductStatus.CONDUCTED;

        String auditRemarks = request.getRemarks();
        if (auditRemarks == null || auditRemarks.isBlank()) {
            auditRemarks = request.getMeetingRemarks();
        }

        // ── Build immutable audit record ──────────────────────────────────────
        MeetingUpdate update = MeetingUpdate.builder()
                .meeting(meeting)
                .updateNumber(nextUpdateNumber)
                .meetingDate(request.getMeetingDate() != null ? request.getMeetingDate() : meeting.getMeetingDate())
                .meetingTime(request.getMeetingTime() != null ? request.getMeetingTime() : meeting.getMeetingTime())
                .meetingMode(request.getMeetingMode() != null ? request.getMeetingMode() : meeting.getMeetingMode())
                .meetingConducted(conductStatus)
                .completedStage(request.getCompletedStage())
                .leadStatus(request.getLeadStatus())
                .clientStatus(request.getClientStatus())
                .remarks(auditRemarks)
                .joinedMeetingWith(request.getJoinedMeetingWith())
                .aloneWith(request.getAloneWith())
                .personName("SELF".equalsIgnoreCase(request.getAloneWith()) ? null : request.getPersonName())
                .position("SELF".equalsIgnoreCase(request.getAloneWith()) ? null : request.getPosition())
                .leaderName(request.getLeaderName())
                .nextPlanDate(request.getNextPlanDate())
                .panNumber(request.getPanNumber())
                .investmentAmount(request.getInvestmentAmount())
                .productType(request.getProductType())
                .discussion(request.getDiscussion())
                .reason(request.getReason())
                .nextPlanTime(request.getNextPlanTime())
                .currentInvestmentCompany(request.getCurrentInvestmentCompany())
                .currentAdvisor(request.getCurrentAdvisor())
                .investmentType(request.getInvestmentType())
                .investmentCompany(request.getInvestmentCompany())
                .currentStage(request.getCurrentStage())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationCapturedAt(capturedAt)
                .locationAccuracy(request.getAccuracy())
                .googleMapsUrl(googleMapsUrl)
                .submittedBy(submittedBy)
                .submittedAt(LocalDateTime.now())
                .build();

        MeetingUpdate savedUpdate = meetingUpdateRepository.save(update);
        log.info("Persisted MeetingUpdate #{} for meeting {}", nextUpdateNumber, meeting.getMeetingCode());

        // ── Apply denormalized updates to parent Meeting ──────────────────────
        if (request.getMeetingDate() != null)     meeting.setMeetingDate(request.getMeetingDate());
        if (request.getMeetingTime() != null)     meeting.setMeetingTime(request.getMeetingTime());
        if (request.getMeetingMode() != null)     meeting.setMeetingMode(request.getMeetingMode());
        if (request.getMeetingLocation() != null)  meeting.setMeetingLocation(request.getMeetingLocation());
        if (request.getDiscussion() != null)      meeting.setDiscussion(request.getDiscussion());
        if (request.getMeetingRemarks() != null)  meeting.setMeetingRemarks(request.getMeetingRemarks());
        if (request.getNextPlanDate() != null)    meeting.setNextMeetingDate(request.getNextPlanDate());
        if (request.getNextPlanTime() != null)    meeting.setNextMeetingTime(request.getNextPlanTime());

        meeting.setMeetingConducted(conductStatus);
        if (conductStatus == MeetingConductStatus.NOT_CONDUCTED) {
            meeting.setMeetingStatus(MeetingStatus.NOT_CONDUCTED);
        } else {
            meeting.setMeetingStatus(MeetingStatus.COMPLETED);
        }
        if (request.getLeadStatus() != null)                  meeting.setLeadStatus(request.getLeadStatus());
        if (request.getReason() != null)                      meeting.setReason(request.getReason());
        if (request.getCurrentInvestmentCompany() != null)    meeting.setCurrentInvestmentCompany(request.getCurrentInvestmentCompany());
        if (request.getCurrentAdvisor() != null)              meeting.setCurrentAdvisor(request.getCurrentAdvisor());
        if (request.getInvestmentType() != null)              meeting.setInvestmentType(request.getInvestmentType());
        if (request.getInvestmentCompany() != null)           meeting.setInvestmentCompany(request.getInvestmentCompany());
        if (request.getCurrentStage() != null)                meeting.setCurrentStage(request.getCurrentStage());
        if (request.getPanNumber() != null)                   meeting.setPanNumber(request.getPanNumber());
        if (request.getInvestmentAmount() != null)            meeting.setInvestmentAmount(request.getInvestmentAmount());
        
        // Persist GPS fields directly to allow optional NULLs
        meeting.setLatitude(request.getLatitude());
        meeting.setLongitude(request.getLongitude());
        meeting.setLocationCapturedAt(capturedAt);
        meeting.setLocationAccuracy(request.getAccuracy());
        meeting.setGoogleMapsUrl(googleMapsUrl);
        meeting.setAddress(request.getAddress());

        // Persist aloneWith fields if provided
        if (request.getAloneWith() != null) {
            meeting.setAloneWith(request.getAloneWith());
            meeting.setPersonName("SELF".equalsIgnoreCase(request.getAloneWith()) ? null : request.getPersonName());
            meeting.setPosition("SELF".equalsIgnoreCase(request.getAloneWith()) ? null : request.getPosition());
        }

        meetingRepository.save(meeting);

        return savedUpdate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingUpdateResponse> getUpdateHistory(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
        List<MeetingUpdate> updates = meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meeting.getId());
        return meetingUpdateMapper.toResponseList(updates);
    }
}
