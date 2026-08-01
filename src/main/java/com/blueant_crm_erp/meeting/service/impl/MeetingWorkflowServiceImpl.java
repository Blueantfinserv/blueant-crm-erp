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
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.WorkflowDecision;
import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.LeadConvertedEvent;
import com.blueant_crm_erp.meeting.event.LeadWorkflowTerminatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingCompletedEvent;
import com.blueant_crm_erp.meeting.event.MeetingUpdatedEvent;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.FollowUpService;
import com.blueant_crm_erp.meeting.service.MeetingDecisionEngine;
import com.blueant_crm_erp.meeting.service.MeetingUpdateService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.validator.MeetingWorkflowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * ============================================================================
 * Meeting Workflow Service Implementation (Orchestrator)
 * ============================================================================
 *
 * Orchestrates the full meeting update workflow.
 *
 * This class is a pure orchestrator — it delegates every business decision
 * to specialized components:
 *
 * MeetingWorkflowValidator → Guard conditions
 * MeetingUpdateService     → Persist immutable audit record
 * MeetingDecisionEngine    → Evaluate outcome → WorkflowDecision (Strategy)
 * FollowUpService          → Auto-create next sequential meeting
 * LeadService              → Update lead status
 * ApplicationEventPublisher → Publish typed domain events
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
    private final MeetingDecisionEngine decisionEngine;
    private final FollowUpService followUpService;
    private final @Lazy LeadService leadService;
    private final ApplicationEventPublisher eventPublisher;

    /** Terminal outcomes that end the workflow and require no follow-up date */
    private static final Set<MeetingOutcome> TERMINAL_OUTCOMES = Set.of(
            MeetingOutcome.CONVERTED,
            MeetingOutcome.SUCCESS,
            MeetingOutcome.NOT_INTERESTED,
            MeetingOutcome.ALREADY_CLIENT,
            MeetingOutcome.REMOVED,
            MeetingOutcome.REJECTED,
            MeetingOutcome.DOCUMENT_PENDING,
            MeetingOutcome.NO_RESPONSE
    );

    @Override
    public MeetingResponse processWorkflow(String meetingCode, MeetingWorkflowRequest request, String currentUserEmail) {
        log.info("[WorkflowOrchestrator] Processing meeting workflow for: {}, by: {}", meetingCode, currentUserEmail);

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
                
        String previousStatus = meeting.getMeetingStatus().name();

        if (request.getMeetingOutcome() == null) {
            log.info("[WorkflowOrchestrator] No outcome provided for meeting: {}. Saving updates only.", meetingCode);
            MeetingUpdate savedUpdate = meetingUpdateService.persistUpdate(meeting, request, currentUserEmail);
            eventPublisher.publishEvent(new MeetingUpdatedEvent(this, meeting, savedUpdate, currentUserEmail));
            return meetingMapper.toResponse(meeting);
        }

        // ── Step 1: Guard Conditions ─────────────────────────────────────────
        workflowValidator.validate(request);
        workflowValidator.validateMeetingState(meeting);

        // ── Step 2: Persist Immutable Audit Record ───────────────────────────
        MeetingUpdate savedUpdate = meetingUpdateService.persistUpdate(meeting, request, currentUserEmail);

        // ── Step 3: Publish MeetingCompleted + MeetingUpdated Events ────────
        eventPublisher.publishEvent(new MeetingCompletedEvent(this, meeting, previousStatus,
                "Meeting completed via workflow", currentUserEmail));
        eventPublisher.publishEvent(new MeetingUpdatedEvent(this, meeting, savedUpdate, currentUserEmail));

        // ── Step 4: Evaluate Decision ────────────────────────────────────────
        WorkflowDecision decision = decisionEngine.evaluate(request.getMeetingOutcome());
        log.info("[WorkflowOrchestrator] Decision for outcome {}: {}", request.getMeetingOutcome(), decision);

        // ── Step 5: Act on Decision (Status Changes Only) ────────────────────
        switch (decision) {
            case CONVERT_LEAD -> {
                changeLeadStatus(meeting, LeadStatus.CONVERTED, LeadStage.INVESTMENT_CONFIRMED,
                        "Converted from meeting: " + meeting.getMeetingCode(), currentUserEmail);
                eventPublisher.publishEvent(new LeadConvertedEvent(this, meeting, previousStatus, currentUserEmail));
                log.info("[WorkflowOrchestrator] Lead {} converted.", meeting.getLead().getLeadCode());
            }
            case TERMINATE_WORKFLOW -> {
                LeadStatus targetStatus = resolveTerminalLeadStatus(request.getMeetingOutcome());
                changeLeadStatus(meeting, targetStatus, meeting.getLead().getLeadStage(),
                        "Workflow terminated. Outcome: " + request.getMeetingOutcome().getDisplayName(), currentUserEmail);
                eventPublisher.publishEvent(new LeadWorkflowTerminatedEvent(this, meeting,
                        request.getMeetingOutcome(), previousStatus, currentUserEmail));
                log.info("[WorkflowOrchestrator] Workflow terminated for lead {}. Status → {}",
                        meeting.getLead().getLeadCode(), targetStatus);
            }
            case HOLD_LEAD -> {
                changeLeadStatus(meeting, LeadStatus.ON_HOLD, meeting.getLead().getLeadStage(),
                        "No response / on hold from meeting: " + meeting.getMeetingCode(), currentUserEmail);
                log.info("[WorkflowOrchestrator] Lead {} put on hold.", meeting.getLead().getLeadCode());
            }
            case DOCUMENT_PENDING -> {
                changeLeadStatus(meeting, LeadStatus.DOCUMENT_PENDING, LeadStage.DOCUMENT_COLLECTION,
                        "Documents pending from meeting: " + meeting.getMeetingCode(), currentUserEmail);
                log.info("[WorkflowOrchestrator] Lead {} marked document pending.", meeting.getLead().getLeadCode());
            }
            case SCHEDULE_FOLLOW_UP -> {
                // No special lead status changes
            }
        }

        // ── Step 6: Create Next Sequential Meeting (Idempotency-Guarded) ────
        int nextSequence = meeting.getMeetingNumber() + 1;
        boolean wasAlreadyCompleted = MeetingStatus.COMPLETED.name().equals(previousStatus);
        boolean nextSequenceExists = meetingRepository.existsByLeadIdAndMeetingNumber(
                meeting.getLead().getId(), nextSequence);

        if (wasAlreadyCompleted || nextSequenceExists) {
            log.info("[WorkflowOrchestrator] Skipping next meeting creation. " +
                     "wasAlreadyCompleted={}, nextSequenceExists={} (sequence {})",
                     wasAlreadyCompleted, nextSequenceExists, nextSequence);
            return meetingMapper.toResponse(meeting);
        }

        java.time.LocalDate nextDate = request.getNextMeetingDate() != null
                ? request.getNextMeetingDate() : java.time.LocalDate.now().plusDays(1);
        java.time.LocalTime nextTime = request.getNextMeetingTime() != null
                ? request.getNextMeetingTime() : java.time.LocalTime.of(10, 0);

        Meeting nextMeeting = followUpService.createFollowUp(
                meeting, nextDate, nextTime, currentUserEmail);
        eventPublisher.publishEvent(new FollowUpCreatedEvent(this, meeting, nextMeeting, currentUserEmail));
        log.info("[WorkflowOrchestrator] Next sequential meeting #{} created: {}",
                 nextMeeting.getMeetingNumber(), nextMeeting.getMeetingCode());

        return meetingMapper.toResponse(nextMeeting);
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

    private LeadStatus resolveTerminalLeadStatus(MeetingOutcome outcome) {
        return switch (outcome) {
            case NOT_INTERESTED -> LeadStatus.NOT_INTERESTED;
            case ALREADY_CLIENT -> LeadStatus.ALREADY_CLIENT;
            case REMOVED        -> LeadStatus.REMOVED;
            case REJECTED       -> LeadStatus.LOST;
            default             -> LeadStatus.LOST;
        };
    }
}
