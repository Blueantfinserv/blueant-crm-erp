package com.blueant_crm_erp.audit.service;

import com.blueant_crm_erp.audit.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {
    Page<AuditLogResponse> searchAuditLogs(String entityName, Long entityId, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
