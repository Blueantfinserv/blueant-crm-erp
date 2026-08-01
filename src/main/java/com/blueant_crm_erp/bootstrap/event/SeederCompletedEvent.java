package com.blueant_crm_erp.bootstrap.event;

import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SeederCompletedEvent extends ApplicationEvent {

    private final String runId;
    private final String correlationId;
    private final String module;
    private final SeederResult seederResult;
    private final LocalDateTime eventTime;

    public SeederCompletedEvent(Object source, String runId, String correlationId, String module, SeederResult seederResult) {
        super(source);
        this.runId = runId;
        this.correlationId = correlationId;
        this.module = module;
        this.seederResult = seederResult;
        this.eventTime = LocalDateTime.now();
    }
}
