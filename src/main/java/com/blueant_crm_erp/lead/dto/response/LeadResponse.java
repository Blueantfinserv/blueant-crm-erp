package com.blueant_crm_erp.lead.dto.response;

import com.blueant_crm_erp.lead.enums.BestTimeToMeet;
import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadPriority;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Lead Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Standard response DTO containing basic lead information.
 * Used in Create, Update and Get APIs.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

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
    private String email;
    private String location;
    private String companyName;
    private String speciality;
    private String clinicAddress;
    private Boolean isPhysicalLead;
    private BestTimeToMeet bestTimeToMeet;

    /**
     * Lead Information
     */
    private LeadSource leadSource;
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

    private String assignedByEmployeeCode;
    private String assignedByEmployeeName;
    private LocalDateTime assignedAt;
    private LocalDate assignmentDate;
    private String assignmentSource;
    private Boolean assignedByCoordinator;
    private String assignmentLabel;

    /**
     * Follow-up Information
     */
    private LocalDate nextPlanDate;

    /**
     * Last Remarks
     */
    private String remarks;

}