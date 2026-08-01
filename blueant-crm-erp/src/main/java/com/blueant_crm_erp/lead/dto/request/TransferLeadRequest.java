package com.blueant_crm_erp.lead.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Transfer Lead Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used to transfer an existing lead from one Sales Person
 * to another.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferLeadRequest {

    /**
     * Lead ID.
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * Current Assigned Sales Person.
     */
    @NotNull(message = "Current assigned user is required.")
    private Long currentAssignedUserId;

    /**
     * New Sales Person.
     */
    @NotNull(message = "New assigned user is required.")
    private Long newAssignedUserId;

    /**
     * Reason for transfer.
     */
    @NotBlank(message = "Transfer reason is required.")
    @Size(max = 500, message = "Transfer reason cannot exceed 500 characters.")
    private String transferReason;

}