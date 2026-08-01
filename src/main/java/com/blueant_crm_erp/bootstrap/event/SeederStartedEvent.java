package com.blueant_crm_erp.bootstrap.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SeederStartedEvent extends ApplicationEvent {

    private final String runId;
    private final String correlationId;
    private final String module;
    private final LocalDateTime eventTime;

    public SeederStartedEvent(Object source, String runId, String correlationId, String module) {
        super(source);
        this.runId = runId;
        this.correlationId = correlationId;
        this.module = module;
        this.eventTime = LocalDateTime.now();
    }
}
