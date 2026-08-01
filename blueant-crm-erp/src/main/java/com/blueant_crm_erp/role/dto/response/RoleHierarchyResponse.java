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
 * ============================================================================
 * Role Hierarchy Response
 * ============================================================================
 *
 * Represents the hierarchy relationship of a role.
 *
 * Example:
 *
 * Director
 *     └── Sales Manager
 *             └── Team Leader
 *                     └── Sales Person
 *
 * Used In:
 * - Role Hierarchy API
 * - Organization Hierarchy
 * - User Reporting Structure
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * ============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleHierarchyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Id.
     */
    @Schema(description = "Role Id", example = "Example Role Id")
    private Long roleId;

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
     * Parent Role Id.
     */
    @Schema(description = "Parent Role Id", example = "Example Parent Role Id")
    private Long parentRoleId;

    /**
     * Parent Role Name.
     */
    @Schema(description = "Parent Role Name", example = "Example Parent Role Name")
    private String parentRoleName;

    /**
     * Hierarchy Level.
     *
     * Example:
     * 1 - Director
     * 2 - Sales Manager
     * 3 - Team Leader
     * 4 - Sales Person
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

    /**
     * Data Access Level.
     */
    @Schema(description = "Data Access Level", example = "Example Data Access Level")
    private DataAccessLevel dataAccessLevel;

    /**
     * Role Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Indicates whether this is a system role.
     */
    @Schema(description = "System Role", example = "Example System Role")
    private Boolean systemRole;

}