package com.blueant_crm_erp.client.event;

import com.blueant_crm_erp.client.entity.Client;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ClientCreatedEvent extends ApplicationEvent {
    
    private final Client client;
    private final String description;
    private final String triggeredBy;

    public ClientCreatedEvent(Object source, Client client, String description, String triggeredBy) {
        super(source);
        this.client = client;
        this.description = description;
        this.triggeredBy = triggeredBy;
    }
}
