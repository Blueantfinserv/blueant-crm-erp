package com.blueant_crm_erp.audit.listener;

import com.blueant_crm_erp.audit.entity.AuditLog;
import com.blueant_crm_erp.audit.event.EntityAuditCreatedEvent;
import com.blueant_crm_erp.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @EventListener
    public void handleAuditCreatedEvent(EntityAuditCreatedEvent event) {
        log.debug("Async persisting audit log for {} {}", event.getEntityName(), event.getEntityId());
        
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName(event.getEntityName())
                    .entityId(Long.parseLong(event.getEntityId()))
                    .actionType(event.getAction())
                    .oldValue(event.getOldState())
                    .newValue(event.getNewState())
                    .actionTime(LocalDateTime.now())
                    .who(event.getPerformedBy())
                    .ipAddress("127.0.0.1")
                    .browser("System Thread")
                    .build();
                    
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist async audit log: {}", e.getMessage());
        }
    }
}
