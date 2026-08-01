package com.blueant_crm_erp.proposal.event;

import com.blueant_crm_erp.proposal.entity.Proposal;

public class ProposalCreatedEvent extends ProposalWorkflowEvent {
    public ProposalCreatedEvent(Object source, Proposal proposal, String description, String triggeredBy) {
        super(source, proposal, "CREATED", description, triggeredBy);
    }
}
