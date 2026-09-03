package com.blueant_crm_erp.lead.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Assign Physical Lead Request
 * ============================================================================
 *
 * Description:
 * Request DTO used by a Sales Coordinator to assign a physical lead to a Sales Person.
 *
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPhysicalLeadRequest {

    /**
     * Target Sales Person Employee Code.
     */
    @NotBlank(message = "Sales Person employee code is required.")
    private String salesPersonEmployeeCode;

    /**
     * Optional assignment remarks/reason.
     */
    private String assignmentReason;
}
