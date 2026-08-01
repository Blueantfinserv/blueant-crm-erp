package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingUpdateResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
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
 * Meeting Update Service Implementation
 * ============================================================================
 *
 * Persists immutable MeetingUpdate audit records and applies denormalized
 * updates to the parent Meeting entity for fast reads.
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

        // ── Build immutable audit record ──────────────────────────────────────
        MeetingUpdate update = MeetingUpdate.builder()
                .meeting(meeting)
                .updateNumber(nextUpdateNumber)
                .meetingDate(request.getMeetingDate())
                .meetingTime(request.getMeetingTime())
                .meetingMode(request.getMeetingMode())
                .meetingConducted(request.getMeetingConducted() != null ? request.getMeetingConducted() : Boolean.FALSE)
                .completedStage(request.getCompletedStage())
                .leadStatus(request.getLeadStatus())
                .clientStatus(request.getClientStatus())
                .remarks(request.getMeetingRemarks())
                .joinedMeetingWith(request.getJoinedMeetingWith())
                .leaderName(request.getLeaderName())
                .nextPlanDate(request.getNextPlanDate())
                .panNumber(request.getPanNumber())
                .investmentAmount(request.getInvestmentAmount())
                .productType(request.getProductType())
                .meetingOutcome(request.getMeetingOutcome())
                .discussion(request.getDiscussion())
                .submittedBy(submittedBy)
                .submittedAt(LocalDateTime.now())
                .build();

        MeetingUpdate savedUpdate = meetingUpdateRepository.save(update);
        log.info("Persisted MeetingUpdate #{} for meeting {}", nextUpdateNumber, meeting.getMeetingCode());

        // ── Apply denormalized updates to parent Meeting ──────────────────────
        if (request.getMeetingDate() != null)     meeting.setMeetingDate(request.getMeetingDate());
        if (request.getMeetingTime() != null)     meeting.setMeetingTime(request.getMeetingTime());
        if (request.getMeetingMode() != null)     meeting.setMeetingMode(request.getMeetingMode());
        if (request.getDiscussion() != null)      meeting.setDiscussion(request.getDiscussion());
        if (request.getMeetingOutcome() != null)  meeting.setMeetingOutcome(request.getMeetingOutcome());
        if (request.getMeetingRemarks() != null)  meeting.setMeetingRemarks(request.getMeetingRemarks());
        if (request.getNextPlanDate() != null)    meeting.setNextMeetingDate(request.getNextPlanDate());

        // Mark meeting as completed ONLY if an outcome is provided (workflow progression)
        if (request.getMeetingOutcome() != null) {
            meeting.setMeetingStatus(MeetingStatus.COMPLETED);
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
