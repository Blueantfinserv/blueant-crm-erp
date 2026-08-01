package com.blueant_crm_erp.common.dto.audit;

import com.blueant_crm_erp.common.dto.reference.ReferenceUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Audit Info DTO
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Common
 *
 * Description:
 * Reusable DTO for providing standard audit representation across the ERP.
 *
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditInfoDto {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private ReferenceUserDto createdBy;
    private ReferenceUserDto updatedBy;

}
