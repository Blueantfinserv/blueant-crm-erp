package com.blueant_crm_erp.meeting.listener;

import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.MeetingCompletedEvent;
import com.blueant_crm_erp.meeting.event.MeetingScheduledEvent;
import com.blueant_crm_erp.meeting.event.MeetingUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ============================================================================
 * Meeting Workflow Listener
 * ============================================================================
 *
 * Handles meeting-specific domain events to maintain internal meeting state
 * consistency and trigger downstream meeting-related processes.
 *
 * Future: handle meeting reminders, SMS confirmations, calendar invites.
 */
@Slf4j
@Component
public class MeetingWorkflowListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingScheduled(MeetingScheduledEvent event) {
        log.info("[MeetingWorkflow] Meeting #{} scheduled: {} | Lead: {}",
                event.getMeeting().getMeetingNumber(),
                event.getMeeting().getMeetingCode(),
                event.getMeeting().getLead() != null ? event.getMeeting().getLead().getLeadCode() : "N/A");
        // TODO: Send calendar invite to sales rep
        // TODO: Send SMS confirmation to client (if phone number available)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingCompleted(MeetingCompletedEvent event) {
        log.info("[MeetingWorkflow] Meeting {} completed. Conducted: {} | Lead Status: {}",
                event.getMeeting().getMeetingCode(),
                event.getMeeting().getMeetingConducted(),
                event.getMeeting().getLeadStatus());
        // TODO: Remove from today's pending meetings queue
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingUpdated(MeetingUpdatedEvent event) {
        log.info("[MeetingWorkflow] Meeting {} received update #{} by {}.",
                event.getMeeting().getMeetingCode(),
                event.getMeetingUpdate().getUpdateNumber(),
                event.getTriggeredBy());
        // TODO: Notify supervisor if investment amount exceeds threshold
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowUpCreated(FollowUpCreatedEvent event) {
        log.info("[MeetingWorkflow] Auto follow-up meeting #{} created: {} | For lead: {}",
                event.getFollowUpMeeting().getMeetingNumber(),
                event.getFollowUpMeeting().getMeetingCode(),
                event.getFollowUpMeeting().getLead().getLeadCode());
        // TODO: Add to tomorrow/next-day meeting queue
    }
}
