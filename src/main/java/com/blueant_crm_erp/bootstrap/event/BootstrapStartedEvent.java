package com.blueant_crm_erp.bootstrap.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class BootstrapStartedEvent extends ApplicationEvent {

    private final String runId;
    private final String correlationId;
    private final LocalDateTime eventTime;

    public BootstrapStartedEvent(Object source, String runId, String correlationId) {
        super(source);
        this.runId = runId;
        this.correlationId = correlationId;
        this.eventTime = LocalDateTime.now();
    }
}
