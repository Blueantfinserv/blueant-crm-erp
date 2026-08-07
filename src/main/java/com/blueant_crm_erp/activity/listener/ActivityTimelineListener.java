package com.blueant_crm_erp.activity.listener;

import com.blueant_crm_erp.activity.entity.ActivityTimeline;
import com.blueant_crm_erp.activity.enums.ActivityType;
import com.blueant_crm_erp.activity.repository.ActivityTimelineRepository;
import com.blueant_crm_erp.client.event.ClientCreatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingWorkflowEvent;
import com.blueant_crm_erp.negotiation.event.NegotiationWorkflowEvent;
import com.blueant_crm_erp.proposal.event.ProposalWorkflowEvent;
import com.blueant_crm_erp.servicerequest.event.ServiceRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityTimelineListener {

    private final ActivityTimelineRepository activityTimelineRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingWorkflowEvent(MeetingWorkflowEvent event) {
        log.info("TimelineListener processing MeetingWorkflowEvent for meeting code: {}", event.getMeeting().getMeetingCode());
        
        ActivityTimeline timeline = ActivityTimeline.builder()
                .lead(event.getMeeting().getLead())
                .activityType(ActivityType.MEETING)
                .referenceId(event.getMeeting().getId())
                .title("Meeting " + event.getEventType())
                .description(event.getDescription())
                .status(event.getEventType())
                .sequenceNumber(event.getMeeting().getMeetingNumber())
                .outcome(event.getMeeting().getLeadStatus() != null ? event.getMeeting().getLeadStatus().name() : (event.getMeeting().getMeetingConducted() != null ? event.getMeeting().getMeetingConducted().name() : null))
                .previousStatus(event.getPreviousStatus())
                .currentStatus(event.getMeeting().getMeetingStatus() != null ? event.getMeeting().getMeetingStatus().name() : null)
                .build();
        
        timeline.setCreatedBy(event.getTriggeredBy());
        timeline.setCreatedAt(LocalDateTime.now());
        
        activityTimelineRepository.save(timeline);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProposalWorkflowEvent(ProposalWorkflowEvent event) {
        log.info("TimelineListener processing ProposalWorkflowEvent for proposal code: {}", event.getProposal().getProposalCode());
        
        ActivityTimeline timeline = ActivityTimeline.builder()
                .lead(event.getProposal().getLead())
                .activityType(ActivityType.PROPOSAL)
                .referenceId(event.getProposal().getId())
                .title("Proposal " + event.getEventType())
                .description(event.getDescription())
                .status(event.getEventType())
                .currentStatus(event.getProposal().getProposalStatus().name())
                .build();
        
        timeline.setCreatedBy(event.getTriggeredBy());
        timeline.setCreatedAt(LocalDateTime.now());
        
        activityTimelineRepository.save(timeline);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNegotiationWorkflowEvent(NegotiationWorkflowEvent event) {
        log.info("TimelineListener processing NegotiationWorkflowEvent for negotiation code: {}", event.getNegotiation().getNegotiationCode());
        
        ActivityTimeline timeline = ActivityTimeline.builder()
                .lead(event.getNegotiation().getProposal().getLead())
                .activityType(ActivityType.NEGOTIATION)
                .referenceId(event.getNegotiation().getId())
                .title("Negotiation " + event.getEventType())
                .description(event.getDescription())
                .status(event.getEventType())
                .currentStatus(event.getNegotiation().getNegotiationStatus().name())
                .build();
        
        timeline.setCreatedBy(event.getTriggeredBy());
        timeline.setCreatedAt(LocalDateTime.now());
        
        activityTimelineRepository.save(timeline);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClientCreatedEvent(ClientCreatedEvent event) {
        log.info("TimelineListener processing ClientCreatedEvent for client code: {}", event.getClient().getClientCode());
        
        ActivityTimeline timeline = ActivityTimeline.builder()
                .lead(event.getClient().getLead())
                .activityType(ActivityType.CLIENT)
                .referenceId(event.getClient().getId())
                .title("Client Created")
                .description(event.getDescription())
                .status("CREATED")
                .currentStatus(event.getClient().getClientStatus().name())
                .build();
        
        timeline.setCreatedBy(event.getTriggeredBy());
        timeline.setCreatedAt(LocalDateTime.now());
        
        activityTimelineRepository.save(timeline);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleServiceRequestCreatedEvent(ServiceRequestCreatedEvent event) {
        log.info("TimelineListener processing ServiceRequestCreatedEvent for SR code: {}", event.getServiceRequest().getSrCode());
        
        ActivityTimeline timeline = ActivityTimeline.builder()
                .lead(event.getServiceRequest().getClient().getLead())
                .activityType(ActivityType.SERVICE_REQUEST)
                .referenceId(event.getServiceRequest().getId())
                .title("Service Request Generated")
                .description(event.getDescription())
                .status("CREATED")
                .currentStatus(event.getServiceRequest().getSrStatus().name())
                .build();
        
        timeline.setCreatedBy(event.getTriggeredBy());
        timeline.setCreatedAt(LocalDateTime.now());
        
        activityTimelineRepository.save(timeline);
    }
}
