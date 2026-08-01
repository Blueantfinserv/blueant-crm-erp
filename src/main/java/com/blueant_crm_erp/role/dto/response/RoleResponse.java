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
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Role Response
 * ============================================================================
 *
 * Response DTO representing Role details.
 *
 * Used In:
 * - Get Role By Id
 * - Create Role
 * - Update Role
 * - Get All Roles
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse implements Serializable {

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
     * Role Description.
     */
    @Schema(description = "Description", example = "Example Description")
    private String description;

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
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Data Access Level.
     */
    @Schema(description = "Data Access Level", example = "Example Data Access Level")
    private DataAccessLevel dataAccessLevel;

    /**
     * Indicates whether this is a system role.
     */
    @Schema(description = "System Role", example = "Example System Role")
    private Boolean systemRole;

    /**
     * Indicates whether this is the default role.
     */
    @Schema(description = "Default Role", example = "Example Default Role")
    private Boolean defaultRole;

    /**
     * Current Role Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Additional Remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks")
    private String remarks;

    /**
     * Record Created By.
     */
    @Schema(description = "Created By", example = "Example Created By")
    private String createdBy;

    /**
     * Record Creation Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Record Last Updated By.
     */
    @Schema(description = "Updated By", example = "Example Updated By")
    private String updatedBy;

    /**
     * Record Last Updated Time.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

}