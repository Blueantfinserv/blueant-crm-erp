package com.blueant_crm_erp.lead.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Lead Dropdown Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Response DTO used for Lead dropdowns and autocomplete.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDropdownResponse {

    /**
     * Lead ID
     */
    private Long leadId;

    /**
     * Lead Code
     */
    private String leadCode;

    /**
     * Unique Lead Identifier
     */
    private String uniqueLeadId;

    /**
     * Client Name
     */
    private String clientName;

    /**
     * Primary Mobile Number
     */
    private String mobileNumber;

    /**
     * Display Label
     * Example:
     * LD000123 - Rahul Sharma (9876543210)
     */
    private String displayLabel;

}