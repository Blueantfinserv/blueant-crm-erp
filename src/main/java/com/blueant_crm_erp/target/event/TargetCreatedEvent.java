package com.blueant_crm_erp.target.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TargetCreatedEvent extends ApplicationEvent {

    private final Long targetId;
    private final Long userId;
    private final String targetMonth;

    public TargetCreatedEvent(Object source, Long targetId, Long userId, String targetMonth) {
        super(source);
        this.targetId = targetId;
        this.userId = userId;
        this.targetMonth = targetMonth;
    }
}
