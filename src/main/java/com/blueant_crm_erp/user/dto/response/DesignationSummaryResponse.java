package com.blueant_crm_erp.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Designation Summary Response
 * =============================================================================
 *
 * Lightweight response DTO representing Designation information.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Designation Listing
 * • Designation Search
 * • Dashboard Tables
 * • Pagination APIs
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Level 1 → Business Head
 * Level 2 → Sales Manager
 * Level 3 → Team Leader
 * Level 4 → Sales Person
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignationSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Designation Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Designation Name.
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Designation Code.
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Hierarchy Level.
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

    /**
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Total Users.
     */
    @Schema(description = "Total Users", example = "Example Total Users")
    private Integer totalUsers;

    /**
     * Designation Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

}