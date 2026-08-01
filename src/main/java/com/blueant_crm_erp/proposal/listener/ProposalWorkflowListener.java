package com.blueant_crm_erp.proposal.listener;

import com.blueant_crm_erp.proposal.event.ProposalAcceptedEvent;
import com.blueant_crm_erp.proposal.event.ProposalCreatedEvent;
import com.blueant_crm_erp.proposal.event.ProposalRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ProposalWorkflowListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCreated(ProposalCreatedEvent event) {
        log.info("[ProposalWorkflow] Proposal created: {} for Lead: {}", 
            event.getProposal().getProposalCode(), 
            event.getProposal().getLead().getLeadCode());
        // Track analytics, notify user
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        log.info("[ProposalWorkflow] Proposal accepted: {}", event.getProposal().getProposalCode());
        // Publish downstream events or log audit
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalRejected(ProposalRejectedEvent event) {
        log.info("[ProposalWorkflow] Proposal rejected: {}", event.getProposal().getProposalCode());
    }
}
