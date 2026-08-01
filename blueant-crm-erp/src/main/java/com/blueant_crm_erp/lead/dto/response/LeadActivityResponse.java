package com.blueant_crm_erp.lead.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Lead Activity Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Response DTO representing a single activity performed on a lead.
 * Used for Lead Timeline / Activity History.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivityResponse {

    /**
     * Activity ID
     */
    private Long activityId;

    /**
     * Lead ID
     */
    private Long leadId;

    /**
     * Lead Code
     */
    private String leadCode;

    /**
     * Activity Type
     * Example:
     * Lead Created
     * Status Updated
     * Meeting Completed
     * Follow-up Scheduled
     * Lead Assigned
     * Lead Transferred
     * Lead Converted
     */
    private String activityType;

    /**
     * Activity Description
     */
    private String activityDescription;

    /**
     * Performed By
     */
    private Long performedById;

    /**
     * Employee Name
     */
    private String performedByName;

    /**
     * Employee Code
     */
    private String performedByEmployeeCode;

    /**
     * Activity Date & Time
     */
    private LocalDateTime activityDateTime;

    /**
     * Additional Remarks
     */
    private String remarks;

}