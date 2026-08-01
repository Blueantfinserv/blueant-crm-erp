package com.blueant_crm_erp.audit.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntityAuditCreatedEvent extends ApplicationEvent {

    private final String action;
    private final String entityName;
    private final String entityId;
    private final String oldState;
    private final String newState;
    private final String performedBy;

    public EntityAuditCreatedEvent(Object source, String action, String entityName, String entityId, 
                                   String oldState, String newState, String performedBy) {
        super(source);
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.oldState = oldState;
        this.newState = newState;
        this.performedBy = performedBy;
    }
}
