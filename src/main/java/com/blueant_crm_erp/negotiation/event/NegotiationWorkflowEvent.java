package com.blueant_crm_erp.negotiation.event;

import com.blueant_crm_erp.negotiation.entity.Negotiation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class NegotiationWorkflowEvent extends ApplicationEvent {

    private final Negotiation negotiation;
    private final String eventType;
    private final String description;
    private final String triggeredBy;

    public NegotiationWorkflowEvent(Object source, Negotiation negotiation, String eventType, String description, String triggeredBy) {
        super(source);
        this.negotiation = negotiation;
        this.eventType = eventType;
        this.description = description;
        this.triggeredBy = triggeredBy;
    }
}
