package com.blueant_crm_erp.client.listener;

import com.blueant_crm_erp.client.event.ClientCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ClientWorkflowListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClientCreated(ClientCreatedEvent event) {
        log.info("[ClientWorkflow] Client created: {} for Lead: {}", 
            event.getClient().getClientCode(), 
            event.getClient().getLead().getLeadCode());
        // Track analytics, notify CRM head
    }
}
