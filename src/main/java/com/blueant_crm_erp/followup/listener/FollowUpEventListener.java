package com.blueant_crm_erp.followup.listener;

import com.blueant_crm_erp.followup.event.FollowUpCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class FollowUpEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowUpCompletedEvent(FollowUpCompletedEvent event) {
        log.info("FollowUp {} completed by {}", event.getFollowUpId(), event.getCompletedBy());
        // Additional downstream logic like notification processing can be added here
    }
}
