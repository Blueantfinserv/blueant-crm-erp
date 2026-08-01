package com.blueant_crm_erp.proposal.event;

import com.blueant_crm_erp.proposal.entity.Proposal;

public class ProposalRejectedEvent extends ProposalWorkflowEvent {
    public ProposalRejectedEvent(Object source, Proposal proposal, String description, String triggeredBy) {
        super(source, proposal, "REJECTED", description, triggeredBy);
    }
}
