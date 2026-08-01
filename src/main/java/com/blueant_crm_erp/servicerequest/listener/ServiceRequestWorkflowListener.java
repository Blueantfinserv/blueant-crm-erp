package com.blueant_crm_erp.servicerequest.listener;

import com.blueant_crm_erp.servicerequest.event.ServiceRequestCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ServiceRequestWorkflowListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onServiceRequestCreated(ServiceRequestCreatedEvent event) {
        log.info("[ServiceRequestWorkflow] Service Request created: {} for Client: {}", 
            event.getServiceRequest().getSrCode(), 
            event.getServiceRequest().getClient().getClientCode());
        // Track analytics, notify assigned CRM
    }
}
