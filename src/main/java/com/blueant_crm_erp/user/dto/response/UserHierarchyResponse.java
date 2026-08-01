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
import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * User Hierarchy Response
 * =============================================================================
 *
 * Complete hierarchy information of a User.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • User Hierarchy API
 * • Reporting Structure API
 * • Team Hierarchy API
 * • Dashboard
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
public class UserHierarchyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id.
     */
    @Schema(description = "User Id", example = "Example User Id")
    private Long userId;

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
     * User Email.
     */
    @Schema(description = "Email", example = "Example Email")
    private String email;

    /**
     * Mobile Number.
     */
    @Schema(description = "Mobile Number", example = "Example Mobile Number")
    private String mobileNumber;

    /**
     * Department.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Designation.
     */
    @Schema(description = "Designation Name", example = "Example Designation Name")
    private String designationName;

    /**
     * Team.
     */
    @Schema(description = "Team Name", example = "Example Team Name")
    private String teamName;

    /**
     * Role.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * User Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

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
     * Reporting Manager Employee Code.
     */
    @Schema(description = "Reporting Manager Code", example = "Example Reporting Manager Code")
    private String reportingManagerCode;

    /**
     * Reporting Manager Designation.
     */
    @Schema(description = "Reporting Manager Designation", example = "Example Reporting Manager Designation")
    private String reportingManagerDesignation;

    /**
     * Total Direct Reporting Users.
     */
    @Schema(description = "Total Reporting Users", example = "Example Total Reporting Users")
    private Integer totalReportingUsers;

    /**
     * Direct Reporting Users.
     */
    @Schema(description = "Reporting Users", example = "Example Reporting Users")
    private List<UserSummaryResponse> reportingUsers;

    /**
     * Created Date.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Updated Date.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

}