package com.blueant_crm_erp.lead.dto.response;

import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadPriority;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.enums.LeadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Lead Detail Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Response DTO containing complete information of a single Lead.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDetailResponse {

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
    private String alternateMobileNumber;
    private String email;
    private String location;
    private String companyName;

    /**
     * Lead Information
     */
    private LeadSource leadSource;
    private LeadType leadType;
    private LeadStatus leadStatus;
    private LeadStage leadStage;
    private LeadPriority priority;
    private DuplicateLeadStatus duplicateLeadStatus;

    /**
     * Assignment Information
     */
    private Long assignedUserId;
    private String assignedEmployeeCode;
    private String assignedEmployeeName;

    private Long leaderId;
    private String leaderName;

    /**
     * Follow-up Information
     */
    private LocalDate nextPlanDate;
    private LocalDate lastCallDate;

    /**
     * Remarks
     */
    private String remarks;

    private com.blueant_crm_erp.common.dto.audit.AuditInfoDto audit;

}