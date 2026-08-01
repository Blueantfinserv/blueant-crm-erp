package com.blueant_crm_erp.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private String performedBy;
    private LocalDateTime performedAt;
}
