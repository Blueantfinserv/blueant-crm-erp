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
 * User Hierarchy Summary Response
 * =============================================================================
 *
 * Lightweight response DTO representing a User in the reporting hierarchy.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • User Hierarchy API
 * • Reporting Structure API
 * • Organization Chart
 * • Dashboard Hierarchy
 * • Team Hierarchy
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head (Rohit)
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
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
public class UserHierarchySummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Employee Code.
     */
    @Schema(description = "Employee Code", example = "Example Employee Code")
    private String employeeCode;

    /**
     * User Full Name.
     */
    @Schema(description = "Full Name", example = "Example Full Name")
    private String fullName;

    /**
     * Role Name.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * Designation Name.
     */
    @Schema(description = "Designation Name", example = "Example Designation Name")
    private String designationName;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Team Name.
     */
    @Schema(description = "Team Name", example = "Example Team Name")
    private String teamName;

    /**
     * Reporting Manager Id.
     */
    @Schema(description = "Reporting Manager Id", example = "Example Reporting Manager Id")
    private Long reportingManagerId;

    /**
     * Reporting Manager Name.
     */
    @Schema(description = "Reporting Manager Name", example = "Example Reporting Manager Name")
    private String reportingManagerName;

    /**
     * Hierarchy Level.
     *
     * Example:
     * 1 = Business Head
     * 2 = Sales Manager
     * 3 = Team Leader
     * 4 = Sales Person
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

    /**
     * Number of Direct Reporting Users.
     */
    @Schema(description = "Total Reporting Users", example = "Example Total Reporting Users")
    private Integer totalReportingUsers;

    /**
     * User Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

}