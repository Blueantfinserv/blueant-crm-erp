package com.blueant_crm_erp.proposal.event;

import com.blueant_crm_erp.proposal.entity.Proposal;

public class ProposalAcceptedEvent extends ProposalWorkflowEvent {
    public ProposalAcceptedEvent(Object source, Proposal proposal, String description, String triggeredBy) {
        super(source, proposal, "ACCEPTED", description, triggeredBy);
    }
}
