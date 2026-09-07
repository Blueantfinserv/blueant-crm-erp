package com.blueant_crm_erp.lead.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Lead Search Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used for searching, filtering, pagination and sorting
 * of lead records.
 *
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LeadSearchRequest extends LeadFilterRequest {

    /**
     * Global Search Keyword
     * Searches by:
     * - Lead Code
     * - Client Name
     * - Mobile Number
     * - Email
     * - Company Name
     */
    private String keyword;

    /**
     * Filter Criteria
     */
    private LeadFilterRequest filter;

    /**
     * Page Number
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Page Size
     */
    @Builder.Default
    private Integer size = 10;

    /**
     * Sort Field
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Sort Direction
     * ASC / DESC
     */
    @Builder.Default
    private String sortDirection = "DESC";

}