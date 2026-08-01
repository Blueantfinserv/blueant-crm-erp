package com.blueant_crm_erp.bootstrap.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class BootstrapCompletedEvent extends ApplicationEvent {

    private final String runId;
    private final String correlationId;
    private final long durationMs;
    private final LocalDateTime eventTime;

    public BootstrapCompletedEvent(Object source, String runId, String correlationId, long durationMs) {
        super(source);
        this.runId = runId;
        this.correlationId = correlationId;
        this.durationMs = durationMs;
        this.eventTime = LocalDateTime.now();
    }
}
