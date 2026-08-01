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
 * Analytics Listener
 * ============================================================================
 *
 * Processes meeting workflow events for analytics and metric tracking.
 * Runs asynchronously after transaction commit to avoid blocking the main flow.
 *
 * Future Implementation: Update Redis counters, analytics database tables,
 * and real-time dashboard metrics.
 */
@Slf4j
@Component
public class AnalyticsListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingCompleted(MeetingCompletedEvent event) {
        log.info("[Analytics] Meeting completed: {} | Outcome: {}",
                event.getMeeting().getMeetingCode(),
                event.getMeeting().getMeetingOutcome());
        // TODO: Increment meeting_completed counter in Redis/analytics store
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingUpdated(MeetingUpdatedEvent event) {
        log.info("[Analytics] Meeting update #{} persisted for: {}",
                event.getMeetingUpdate().getUpdateNumber(),
                event.getMeeting().getMeetingCode());
        // TODO: Update average meeting update frequency
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowUpCreated(FollowUpCreatedEvent event) {
        log.info("[Analytics] Follow-up meeting #{} created: {}",
                event.getFollowUpMeeting().getMeetingNumber(),
                event.getFollowUpMeeting().getMeetingCode());
        // TODO: Track follow-up creation rate per lead
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadConverted(LeadConvertedEvent event) {
        log.info("[Analytics] Lead converted via meeting: {} | Lead: {}",
                event.getMeeting().getMeetingCode(),
                event.getMeeting().getLead().getLeadCode());
        // TODO: Update conversion metrics, avg meetings to convert
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowTerminated(LeadWorkflowTerminatedEvent event) {
        log.info("[Analytics] Workflow terminated for lead: {} | Outcome: {}",
                event.getMeeting().getLead().getLeadCode(),
                event.getTerminalOutcome());
        // TODO: Track termination reasons by category
    }

    /**
     * Backward-compatible handler for generic MeetingWorkflowEvent.
     * Handles events not covered by specific typed handlers above.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingWorkflowEvent(MeetingWorkflowEvent event) {
        if (event.getClass() == MeetingWorkflowEvent.class) {
            log.info("[Analytics] Generic MeetingWorkflowEvent: meeting {} (Event: {})",
                    event.getMeeting().getMeetingCode(), event.getEventType());
        }
    }
}
