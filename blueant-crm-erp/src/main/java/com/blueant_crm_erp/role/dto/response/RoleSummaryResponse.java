package com.blueant_crm_erp.role.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.DataAccessLevel;
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
 * Role Summary Response
 * =============================================================================
 *
 * Lightweight response DTO representing summary information
 * of a role.
 *
 * Used In:
 * - Role Listing API
 * - Dropdown APIs
 * - Search Results
 * - Dashboard
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Identifier.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Role Name.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * Unique Role Code.
     */
    @Schema(description = "Role Code", example = "Example Role Code")
    private String roleCode;

    /**
     * Hierarchy Level.
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

    /**
     * Data Access Level.
     */
    @Schema(description = "Data Access Level", example = "Example Data Access Level")
    private DataAccessLevel dataAccessLevel;

    /**
     * Current Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Total Users assigned to this role.
     */
    @Schema(description = "Total Users", example = "Example Total Users")
    private Long totalUsers;

    /**
     * Total Permissions assigned to this role.
     */
    @Schema(description = "Total Permissions", example = "Example Total Permissions")
    private Long totalPermissions;

    /**
     * Indicates whether this is a default role.
     */
    @Schema(description = "Default Role", example = "Example Default Role")
    private Boolean defaultRole;

    /**
     * Indicates whether this is a system role.
     */
    @Schema(description = "System Role", example = "Example System Role")
    private Boolean systemRole;

}