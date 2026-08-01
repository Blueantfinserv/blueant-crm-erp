package com.blueant_crm_erp.negotiation.listener;

import com.blueant_crm_erp.negotiation.event.NegotiationCompletedEvent;
import com.blueant_crm_erp.negotiation.event.NegotiationStartedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class NegotiationWorkflowListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNegotiationStarted(NegotiationStartedEvent event) {
        log.info("[NegotiationWorkflow] Negotiation started: {} for Proposal: {}", 
            event.getNegotiation().getNegotiationCode(), 
            event.getNegotiation().getProposal().getProposalCode());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNegotiationCompleted(NegotiationCompletedEvent event) {
        log.info("[NegotiationWorkflow] Negotiation completed: {}", event.getNegotiation().getNegotiationCode());
    }
}
