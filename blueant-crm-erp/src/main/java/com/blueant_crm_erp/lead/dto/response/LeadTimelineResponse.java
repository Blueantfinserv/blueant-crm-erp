package com.blueant_crm_erp.lead.dto.response;

import com.blueant_crm_erp.lead.enums.LeadPriority;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================================
 * Lead Timeline Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Response DTO containing complete timeline information of a Lead.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadTimelineResponse {

    /**
     * Lead Information
     */
    private Long leadId;
    private String leadCode;
    private String uniqueLeadId;

    /**
     * Client Information
     */
    private String clientName;
    private String mobileNumber;

    /**
     * Current Lead Status
     */
    private LeadStatus currentStatus;
    private LeadStage currentStage;
    private LeadPriority currentPriority;

    /**
     * Follow-up Information
     */
    private LocalDate nextPlanDate;
    private LocalDate lastCallDate;

    /**
     * Timeline Activities
     */
    private List<LeadActivityResponse> activities;

}