package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Assign Role Request
 * =============================================================================
 *
 * Request DTO used to assign or change a Role of an existing User.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Super Admin
 * • Admin
 * • Business Head
 *
 * Example
 * -----------------------------------------------------------------------------
 * User
 *      ↓
 * Sales Person
 *      ↓
 * Team Leader
 *      ↓
 * Sales Manager
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
public class AssignRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Id to assign.
     */
    @NotNull(message = "Role Id is required.")
    @Schema(description = "Role Id", example = "Example Role Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    /**
     * Effective From.
     *
     * Example:
     * Immediate
     * Promotion Date
     */
    @Schema(description = "Effective From", example = "Example Effective From", requiredMode = Schema.RequiredMode.REQUIRED)
    private String effectiveFrom;

    /**
     * Reason for role assignment.
     */
    @Size(
            max = 500,
            message = "Reason must not exceed 500 characters."
    )
    @Schema(description = "Reason", example = "Example Reason", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

}