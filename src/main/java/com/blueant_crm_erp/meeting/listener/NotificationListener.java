package com.blueant_crm_erp.meeting.listener;

import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.LeadConvertedEvent;
import com.blueant_crm_erp.meeting.event.LeadWorkflowTerminatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingCompletedEvent;
import com.blueant_crm_erp.meeting.event.MeetingWorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ============================================================================
 * Notification Listener
 * ============================================================================
 *
 * Processes meeting workflow domain events to trigger notifications.
 * Future: Sends push notifications, SMS, WhatsApp, and email alerts.
 */
@Slf4j
@Component
public class NotificationListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingCompleted(MeetingCompletedEvent event) {
        log.info("[Notification] Meeting completed: {} | Notifying team lead and CRM.",
                event.getMeeting().getMeetingCode());
        // TODO: Notify team leader via push notification
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowUpCreated(FollowUpCreatedEvent event) {
        log.info("[Notification] Follow-up meeting #{} scheduled: {} | Notifying assigned rep.",
                event.getFollowUpMeeting().getMeetingNumber(),
                event.getFollowUpMeeting().getMeetingCode());
        // TODO: Send meeting reminder notification to sales rep
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadConverted(LeadConvertedEvent event) {
        log.info("[Notification] Lead converted: {} | Notifying management.",
                event.getMeeting().getLead().getLeadCode());
        // TODO: Send conversion celebration notification + trigger client onboarding
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowTerminated(LeadWorkflowTerminatedEvent event) {
        log.info("[Notification] Workflow terminated for lead: {} | Status: {}",
                event.getMeeting().getLead().getLeadCode(),
                event.getTerminalStatus());
        // TODO: Notify team that lead is closed
    }

    /** Backward-compatible handler */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingWorkflowEvent(MeetingWorkflowEvent event) {
        if (event.getClass() == MeetingWorkflowEvent.class) {
            log.info("[Notification] MeetingWorkflowEvent for meeting: {} (Event: {})",
                    event.getMeeting().getMeetingCode(), event.getEventType());
        }
    }
}
