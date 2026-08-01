package com.blueant_crm_erp.role.dto.response;

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

/**
 * =============================================================================
 * Role Permission Response
 * =============================================================================
 *
 * Response DTO representing a permission assigned to a role.
 *
 * Used In:
 * - Get Permissions By Role
 * - Assign Permission
 * - Remove Permission
 * - Role Details API
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
public class RolePermissionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Permission Mapping Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Role Identifier.
     */
    @Schema(description = "Role Id", example = "Example Role Id")
    private Long roleId;

    /**
     * Role Name.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * Permission Identifier.
     */
    @Schema(description = "Permission Id", example = "Example Permission Id")
    private Long permissionId;

    /**
     * Permission Name.
     */
    @Schema(description = "Permission Name", example = "Example Permission Name")
    private String permissionName;

    /**
     * Unique Permission Code.
     */
    @Schema(description = "Permission Code", example = "Example Permission Code")
    private String permissionCode;

    /**
     * Module Name.
     *
     * Example:
     * SALES
     * CRM
     * HR
     * INSURANCE
     */
    @Schema(description = "Module Name", example = "Example Module Name")
    private String moduleName;

    /**
     * Permission Description.
     */
    @Schema(description = "Description", example = "Example Description")
    private String description;

    /**
     * Current Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Assigned By.
     */
    @Schema(description = "Assigned By", example = "Example Assigned By")
    private String assignedBy;

    /**
     * Permission Assigned Time.
     */
    @Schema(description = "Assigned At", example = "Example Assigned At")
    private LocalDateTime assignedAt;

}