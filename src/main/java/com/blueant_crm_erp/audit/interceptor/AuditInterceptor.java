package com.blueant_crm_erp.audit.interceptor;

import com.blueant_crm_erp.audit.event.EntityAuditCreatedEvent;
import com.blueant_crm_erp.common.base.BaseEntity;
import com.blueant_crm_erp.util.security.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
public class AuditInterceptor extends EmptyInterceptor {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseEntity) {
            logAudit((BaseEntity) entity, "INSERT", null, state, propertyNames);
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseEntity) {
            logAudit((BaseEntity) entity, "UPDATE", previousState, currentState, propertyNames);
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseEntity) {
            logAudit((BaseEntity) entity, "DELETE", state, null, propertyNames);
        }
        super.onDelete(entity, id, state, propertyNames, types);
    }

    private void logAudit(BaseEntity entity, String action, Object[] previousState, Object[] currentState, String[] propertyNames) {
        try {
            java.util.Map<String, Object> oldValMap = extractState(previousState, propertyNames);
            java.util.Map<String, Object> newValMap = extractState(currentState, propertyNames);

            String oldValJson = oldValMap != null ? objectMapper.writeValueAsString(oldValMap) : null;
            String newValJson = newValMap != null ? objectMapper.writeValueAsString(newValMap) : null;
            
            String performedBy = SecurityUtil.getCurrentUsername();
            if (performedBy == null) {
                performedBy = "SYSTEM";
            }

            EntityAuditCreatedEvent event = new EntityAuditCreatedEvent(
                    this,
                    action,
                    entity.getClass().getSimpleName(),
                    String.valueOf(entity.getId() != null ? entity.getId() : 0L),
                    oldValJson,
                    newValJson,
                    performedBy
            );
            
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish audit event", e);
        }
    }

    private java.util.Map<String, Object> extractState(Object[] state, String[] propertyNames) {
        if (state == null || propertyNames == null) {
            return null;
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        for (int i = 0; i < propertyNames.length; i++) {
            Object val = state[i];
            if (val instanceof BaseEntity) {
                map.put(propertyNames[i], ((BaseEntity) val).getId());
            } else if (val instanceof java.util.Collection) {
                map.put(propertyNames[i], "[Collection]");
            } else {
                map.put(propertyNames[i], val);
            }
        }
        return map;
    }
}
