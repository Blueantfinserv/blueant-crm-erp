package com.blueant_crm_erp.meeting.listener;

import com.blueant_crm_erp.meeting.event.FollowUpCreatedEvent;
import com.blueant_crm_erp.meeting.event.LeadConvertedEvent;
import com.blueant_crm_erp.meeting.event.LeadWorkflowTerminatedEvent;
import com.blueant_crm_erp.proposal.event.ProposalAcceptedEvent;
import com.blueant_crm_erp.client.service.ClientWorkflowService;
import com.blueant_crm_erp.servicerequest.service.ServiceRequestWorkflowService;
import com.blueant_crm_erp.client.entity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ============================================================================
 * Lead Workflow Listener
 * ============================================================================
 *
 * Handles meeting domain events that drive the Lead's lifecycle transitions.
 * This listener ensures the Lead module stays in sync with the meeting workflow
 * without creating a tight coupling between modules.
 *
 * Handles transitions to Client and Service Request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadWorkflowListener {

    private final ClientWorkflowService clientWorkflowService;
    private final ServiceRequestWorkflowService serviceRequestWorkflowService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowUpCreated(FollowUpCreatedEvent event) {
        log.info("[LeadWorkflow] Follow-up #{} created for lead: {} | Next meeting: {}",
                event.getFollowUpMeeting().getMeetingNumber(),
                event.getFollowUpMeeting().getLead().getLeadCode(),
                event.getFollowUpMeeting().getMeetingCode());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadConverted(LeadConvertedEvent event) {
        log.info("[LeadWorkflow] Lead converted: {} | Initiating client onboarding workflow.",
                event.getMeeting().getLead().getLeadCode());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowTerminated(LeadWorkflowTerminatedEvent event) {
        log.info("[LeadWorkflow] Lead {} workflow terminated. Terminal outcome: {}. Archiving lead.",
                event.getMeeting().getLead().getLeadCode(),
                event.getTerminalOutcome());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        log.info("[LeadWorkflow] Proposal accepted for lead: {}. Triggering Client and SR creation.", event.getProposal().getLead().getLeadCode());
        
        Client client = clientWorkflowService.createClientFromLead(
            event.getProposal().getLead(), 
            null, 
            event.getTriggeredBy()
        );
        
        serviceRequestWorkflowService.generateServiceRequest(
            client, 
            event.getProposal().getInvestmentAmount(), 
            event.getProposal().getProductType(), 
            event.getTriggeredBy()
        );
    }
}
