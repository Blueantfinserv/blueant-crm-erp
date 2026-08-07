package com.blueant_crm_erp.meeting.listener;

import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.LeadConvertedEvent;
import com.blueant_crm_erp.meeting.event.LeadWorkflowTerminatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingCompletedEvent;
import com.blueant_crm_erp.meeting.event.MeetingUpdatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingWorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ============================================================================
 * Audit Listener
 * ============================================================================
 *
 * Processes all meeting workflow domain events for compliance audit logging.
 * Every significant workflow action is captured with full context.
 *
 * Future implementation: Forward to central audit service / append-only log table.
 */
@Slf4j
@Component
public class AuditListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingCompleted(MeetingCompletedEvent event) {
        log.info("[Audit] MEETING_COMPLETED | Meeting: {} | PreviousStatus: {} | By: {}",
                event.getMeeting().getMeetingCode(),
                event.getPreviousStatus(),
                event.getTriggeredBy());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingUpdated(MeetingUpdatedEvent event) {
        log.info("[Audit] MEETING_UPDATED | Meeting: {} | UpdateNumber: {} | Conducted: {} | LeadStatus: {} | By: {}",
                event.getMeeting().getMeetingCode(),
                event.getMeetingUpdate().getUpdateNumber(),
                event.getMeetingUpdate().getMeetingConducted(),
                event.getMeetingUpdate().getLeadStatus(),
                event.getTriggeredBy());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowUpCreated(FollowUpCreatedEvent event) {
        log.info("[Audit] FOLLOW_UP_CREATED | FollowUpMeeting: {} | MeetingNumber: {} | By: {}",
                event.getFollowUpMeeting().getMeetingCode(),
                event.getFollowUpMeeting().getMeetingNumber(),
                event.getTriggeredBy());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadConverted(LeadConvertedEvent event) {
        log.info("[Audit] LEAD_CONVERTED | Lead: {} | Via Meeting: {} | By: {}",
                event.getMeeting().getLead().getLeadCode(),
                event.getMeeting().getMeetingCode(),
                event.getTriggeredBy());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowTerminated(LeadWorkflowTerminatedEvent event) {
        log.info("[Audit] WORKFLOW_TERMINATED | Lead: {} | TerminalStatus: {} | By: {}",
                event.getMeeting().getLead().getLeadCode(),
                event.getTerminalStatus(),
                event.getTriggeredBy());
    }

    /** Backward-compatible handler */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingWorkflowEvent(MeetingWorkflowEvent event) {
        if (event.getClass() == MeetingWorkflowEvent.class) {
            log.info("[Audit] MeetingWorkflowEvent | Meeting: {} | Event: {} | By: {}",
                    event.getMeeting().getMeetingCode(), event.getEventType(), event.getTriggeredBy());
        }
    }
}
