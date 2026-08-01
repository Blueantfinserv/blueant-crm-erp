package com.blueant_crm_erp.audit.specification;

import com.blueant_crm_erp.audit.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AuditSpecification {

    public static Specification<AuditLog> hasEntityName(String entityName) {
        return (root, query, criteriaBuilder) ->
                entityName == null ? null : criteriaBuilder.equal(root.get("entityName"), entityName);
    }

    public static Specification<AuditLog> hasEntityId(Long entityId) {
        return (root, query, criteriaBuilder) ->
                entityId == null ? null : criteriaBuilder.equal(root.get("entityId"), entityId);
    }
    
    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, criteriaBuilder) ->
                action == null ? null : criteriaBuilder.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> betweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("performedAt"), startDate, endDate);
            }
            return null;
        };
    }
}
