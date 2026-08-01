package com.blueant_crm_erp.negotiation.event;

import com.blueant_crm_erp.negotiation.entity.Negotiation;

public class NegotiationCompletedEvent extends NegotiationWorkflowEvent {
    public NegotiationCompletedEvent(Object source, Negotiation negotiation, String description, String triggeredBy) {
        super(source, negotiation, "COMPLETED", description, triggeredBy);
    }
}
