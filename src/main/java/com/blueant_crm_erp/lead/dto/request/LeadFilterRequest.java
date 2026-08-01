package com.blueant_crm_erp.lead.dto.request;

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

/**
 * ============================================================================
 * Lead Filter Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * DTO used to filter lead records.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadFilterRequest {

    /**
     * Client Name
     */
    private String clientName;

    /**
     * Mobile Number
     */
    private String mobileNumber;

    /**
     * Lead Code
     */
    private String leadCode;

    /**
     * Lead Status
     */
    private LeadStatus leadStatus;

    /**
     * Lead Stage
     */
    private LeadStage leadStage;

    /**
     * Lead Priority
     */
    private LeadPriority leadPriority;

    /**
     * Lead Source
     */
    private LeadSource leadSource;

    /**
     * Duplicate Lead Status
     */
    private DuplicateLeadStatus duplicateLeadStatus;

    /**
     * Assigned Sales Person ID
     */
    private Long assignedUserId;

    /**
     * Team Leader ID
     */
    private Long leaderId;

    /**
     * Created Date Range
     */
    private LocalDate fromDate;

    private LocalDate toDate;

}