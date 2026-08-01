package com.blueant_crm_erp.lead.dto.request;

import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ============================================================================
 * Update Lead Status Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used to update the current status and stage of a lead.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadStatusRequest {

    /**
     * Lead ID
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * Current Lead Status
     */
    @NotNull(message = "Lead status is required.")
    private LeadStatus leadStatus;

    /**
     * Current Lead Stage
     */
    @NotNull(message = "Lead stage is required.")
    private LeadStage leadStage;

    /**
     * Next Follow-up / Plan Date
     */
    private LocalDate nextPlanDate;

    /**
     * Last Client Call Date
     */
    private LocalDate lastCallDate;

    /**
     * Remarks
     */
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

}