package com.blueant_crm_erp.audit.service.impl;

import com.blueant_crm_erp.audit.dto.response.AuditLogResponse;
import com.blueant_crm_erp.audit.entity.AuditLog;
import com.blueant_crm_erp.audit.mapper.AuditMapper;
import com.blueant_crm_erp.audit.repository.AuditLogRepository;
import com.blueant_crm_erp.audit.service.AuditService;
import com.blueant_crm_erp.audit.specification.AuditSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchAuditLogs(String entityName, Long entityId, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<AuditLog> spec = Specification.where(AuditSpecification.hasEntityName(entityName))
                .and(AuditSpecification.hasEntityId(entityId))
                .and(AuditSpecification.hasAction(action))
                .and(AuditSpecification.betweenDates(startDate, endDate));
                
        return auditLogRepository.findAll(spec, pageable).map(auditMapper::toResponse);
    }
}
