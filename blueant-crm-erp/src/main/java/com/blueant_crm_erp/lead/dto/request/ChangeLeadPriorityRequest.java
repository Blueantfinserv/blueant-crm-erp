package com.blueant_crm_erp.lead.dto.request;

import com.blueant_crm_erp.lead.enums.LeadPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Change Lead Priority Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used to change the priority of an existing lead.
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeLeadPriorityRequest {

    /**
     * Lead ID.
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * New Priority.
     */
    @NotNull(message = "Lead priority is required.")
    private LeadPriority leadPriority;

    /**
     * Reason for changing priority.
     */
    @Size(max = 500, message = "Reason cannot exceed 500 characters.")
    private String reason;

}