package com.blueant_crm_erp.bootstrap.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SeederFailedEvent extends ApplicationEvent {

    private final String runId;
    private final String correlationId;
    private final String module;
    private final Throwable exception;
    private final LocalDateTime eventTime;

    public SeederFailedEvent(Object source, String runId, String correlationId, String module, Throwable exception) {
        super(source);
        this.runId = runId;
        this.correlationId = correlationId;
        this.module = module;
        this.exception = exception;
        this.eventTime = LocalDateTime.now();
    }
}
