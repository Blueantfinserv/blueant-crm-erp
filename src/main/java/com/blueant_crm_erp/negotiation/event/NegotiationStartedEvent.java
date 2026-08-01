package com.blueant_crm_erp.negotiation.event;

import com.blueant_crm_erp.negotiation.entity.Negotiation;

public class NegotiationStartedEvent extends NegotiationWorkflowEvent {
    public NegotiationStartedEvent(Object source, Negotiation negotiation, String description, String triggeredBy) {
        super(source, negotiation, "STARTED", description, triggeredBy);
    }
}
