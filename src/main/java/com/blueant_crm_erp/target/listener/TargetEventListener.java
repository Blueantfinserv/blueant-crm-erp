package com.blueant_crm_erp.target.listener;

import com.blueant_crm_erp.target.event.TargetCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TargetEventListener {

    @Async
    @EventListener
    public void handleTargetCreatedEvent(TargetCreatedEvent event) {
        log.info("New target created for user {} for month {}", event.getUserId(), event.getTargetMonth());
        // Custom logic to notify the user
    }
}
