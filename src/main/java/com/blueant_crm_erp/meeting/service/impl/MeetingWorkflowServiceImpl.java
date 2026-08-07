package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.lead.dto.request.UpdateLeadStatusRequest;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.LeadConvertedEvent;
import com.blueant_crm_erp.meeting.event.LeadWorkflowTerminatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingCompletedEvent;
import com.blueant_crm_erp.meeting.event.MeetingUpdatedEvent;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.FollowUpService;
import com.blueant_crm_erp.meeting.service.MeetingUpdateService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.validator.MeetingWorkflowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * Meeting Workflow Service Implementation (Redesigned & Cleaned)
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingWorkflowServiceImpl implements MeetingWorkflowService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;
    private final MeetingWorkflowValidator workflowValidator;
    private final MeetingUpdateService meetingUpdateService;
    private final FollowUpService followUpService;
    private final @Lazy LeadService leadService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MeetingResponse processWorkflow(String meetingCode, MeetingWorkflowRequest request, String currentUserEmail) {
        log.info("[WorkflowOrchestrator] Processing meeting workflow for: {}, by: {}", meetingCode, currentUserEmail);

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
                
        String previousStatus = meeting.getMeetingStatus().name();

        // ── Step 1: Guard Conditions ─────────────────────────────────────────
        workflowValidator.validate(request);
        workflowValidator.validateMeetingState(meeting);

        // ── Step 2: Persist Immutable Audit Record ───────────────────────────
        MeetingUpdate savedUpdate = meetingUpdateService.persistUpdate(meeting, request, currentUserEmail);

        // ── Step 3: Publish MeetingCompleted + MeetingUpdated Events ────────
        if (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED) {
            eventPublisher.publishEvent(new MeetingUpdatedEvent(this, meeting, savedUpdate, currentUserEmail));
        } else {
            eventPublisher.publishEvent(new MeetingCompletedEvent(this, meeting, previousStatus,
                    "Meeting completed via workflow", currentUserEmail));
            eventPublisher.publishEvent(new MeetingUpdatedEvent(this, meeting, savedUpdate, currentUserEmail));
        }

        // ── Step 4: Act on Workflow Transitions ──────────────────────────────
        if (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED) {
            // CASE 1: Not Conducted
            changeLeadStatus(meeting, LeadStatus.FOLLOW_UP_PENDING, meeting.getLead().getLeadStage(),
                    "Meeting not conducted. Lead status updated to Follow-Up Pending.", currentUserEmail);
            log.info("[WorkflowOrchestrator] Meeting not conducted. Lead {} status set to FOLLOW_UP_PENDING.",
                    meeting.getLead().getLeadCode());
        } else {
            // CASE 2: Conducted
            switch (request.getLeadStatus()) {
                case ALREADY_CLIENT -> {
                    changeLeadStatus(meeting, LeadStatus.ALREADY_CLIENT, meeting.getLead().getLeadStage(),
                            request.getMeetingRemarks(), currentUserEmail);
                    eventPublisher.publishEvent(new LeadWorkflowTerminatedEvent(this, meeting,
                            MeetingLeadStatus.ALREADY_CLIENT, previousStatus, currentUserEmail));
                    log.info("[WorkflowOrchestrator] Lead {} marked ALREADY_CLIENT.", meeting.getLead().getLeadCode());
                }
                case CONVERTED_CLIENT -> {
                    changeLeadStatus(meeting, LeadStatus.CONVERTED, LeadStage.INVESTMENT_CONFIRMED,
                            "Converted client from meeting: " + meeting.getMeetingCode(), currentUserEmail);
                    eventPublisher.publishEvent(new LeadConvertedEvent(this, meeting, previousStatus, currentUserEmail));
                    log.info("[WorkflowOrchestrator] Lead {} converted.", meeting.getLead().getLeadCode());
                }
                case REMOVE_CLIENT -> {
                    changeLeadStatus(meeting, LeadStatus.REMOVED, meeting.getLead().getLeadStage(),
                            "Lead removed. Reason: " + request.getReason(), currentUserEmail);
                    eventPublisher.publishEvent(new LeadWorkflowTerminatedEvent(this, meeting,
                            MeetingLeadStatus.REMOVE_CLIENT, previousStatus, currentUserEmail));
                    log.info("[WorkflowOrchestrator] Lead {} removed.", meeting.getLead().getLeadCode());
                }
                case CLIENT_NOT_INTERESTED -> {
                    changeLeadStatus(meeting, LeadStatus.NOT_INTERESTED, meeting.getLead().getLeadStage(),
                            "Lead not interested. Reason: " + request.getReason(), currentUserEmail);
                    eventPublisher.publishEvent(new LeadWorkflowTerminatedEvent(this, meeting,
                            MeetingLeadStatus.CLIENT_NOT_INTERESTED, previousStatus, currentUserEmail));
                    log.info("[WorkflowOrchestrator] Lead {} marked NOT_INTERESTED.", meeting.getLead().getLeadCode());
                }
                case WORK_IN_PROGRESS -> {
                    changeLeadStatus(meeting, LeadStatus.WORK_IN_PROGRESS, meeting.getLead().getLeadStage(),
                            "Meeting conducted. Status updated to Work In Progress.", currentUserEmail);
                    log.info("[WorkflowOrchestrator] Lead {} marked WORK_IN_PROGRESS.", meeting.getLead().getLeadCode());
                }
            }
        }

        // ── Step 5: Create Next Sequential Meeting (If applicable) ───────────
        boolean shouldScheduleFollowUp = (request.getMeetingConducted() == MeetingConductStatus.NOT_CONDUCTED)
                || (request.getMeetingConducted() == MeetingConductStatus.CONDUCTED && request.getLeadStatus() == MeetingLeadStatus.WORK_IN_PROGRESS);

        if (shouldScheduleFollowUp) {
            int nextSequence = meeting.getMeetingNumber() + 1;
            boolean wasAlreadyCompleted = MeetingStatus.COMPLETED.name().equals(previousStatus) || MeetingStatus.NOT_CONDUCTED.name().equals(previousStatus);
            boolean nextSequenceExists = meetingRepository.existsByLeadIdAndMeetingNumber(
                    meeting.getLead().getId(), nextSequence);

            if (!wasAlreadyCompleted && !nextSequenceExists) {
                java.time.LocalDate nextDate = request.getNextPlanDate() != null
                        ? request.getNextPlanDate() : java.time.LocalDate.now().plusDays(1);
                java.time.LocalTime nextTime = request.getNextPlanTime() != null
                        ? request.getNextPlanTime() : java.time.LocalTime.of(10, 0);

                Meeting nextMeeting = followUpService.createFollowUp(
                        meeting, nextDate, nextTime, currentUserEmail);
                eventPublisher.publishEvent(new FollowUpCreatedEvent(this, meeting, nextMeeting, currentUserEmail));
                log.info("[WorkflowOrchestrator] Next sequential meeting #{} created: {}",
                         nextMeeting.getMeetingNumber(), nextMeeting.getMeetingCode());

                return meetingMapper.toResponse(nextMeeting);
            }
        }

        return meetingMapper.toResponse(meeting);
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    private void changeLeadStatus(Meeting meeting, LeadStatus status, LeadStage stage,
                                  String remarks, String currentUserEmail) {
        UpdateLeadStatusRequest req = UpdateLeadStatusRequest.builder()
                .leadId(meeting.getLead().getId())
                .leadStatus(status)
                .leadStage(stage)
                .remarks(remarks)
                .build();
        leadService.changeStatus(req, currentUserEmail);
    }
}
