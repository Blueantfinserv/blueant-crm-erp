package com.blueant_crm_erp.role.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * ============================================================================
 * Assign Permission Request
 * ============================================================================
 *
 * Request DTO used to assign one or more permissions to a role.
 *
 * Example:
 *
 * {
 *   "roleId": 1,
 *   "permissionIds": [2,3,4,5]
 * }
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
public class AssignPermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Identifier.
     */
    @NotNull(message = "Role id is required.")
    @Schema(description = "Role Id", example = "Example Role Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    /**
     * Permission identifiers to be assigned.
     */
    @NotEmpty(message = "At least one permission must be selected.")
    @Schema(description = "Permission Ids", example = "Example Permission Ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;

}