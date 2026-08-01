package com.blueant_crm_erp.lead.event;

import com.blueant_crm_erp.lead.entity.Lead;
import org.springframework.context.ApplicationEvent;

public class LeadCreatedEvent extends ApplicationEvent {

    private final Lead lead;

    public LeadCreatedEvent(Object source, Lead lead) {
        super(source);
        this.lead = lead;
    }

    public Lead getLead() {
        return lead;
    }
}
