package com.blueant_crm_erp.lead.dto.response;

import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadPriority;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ============================================================================
 * Lead Summary Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Lightweight response DTO used for Lead listing, search results,
 * pagination and dashboard views.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSummaryResponse {

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
    private String companyName;
    private String location;

    /**
     * Lead Information
     */
    private LeadStatus leadStatus;
    private LeadStage leadStage;
    private LeadPriority priority;
    private DuplicateLeadStatus duplicateLeadStatus;

    /**
     * Assignment Information
     */
    private String assignedEmployeeName;
    private String leaderName;

    /**
     * Follow-up Information
     */
    private LocalDate nextPlanDate;

    /**
     * Last Remarks
     */
    private String remarks;

}