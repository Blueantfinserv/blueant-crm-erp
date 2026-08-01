package com.blueant_crm_erp.role.dto.request;

import com.blueant_crm_erp.role.constant.RoleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ============================================================================
 * Update Role Request
 * ============================================================================
 *
 * Request DTO used to update an existing role.
 *
 * API:
 * PUT /api/v1/roles/{roleId}
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
@Schema(
        name = "UpdateRoleRequest",
        description = "Request payload used to update an existing role."
)
public class UpdateRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(
            description = "Role name",
            example = "Sales Manager",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Role name is required.")
    @Size(
            min = RoleConstants.ROLE_NAME_MIN_LENGTH,
            max = RoleConstants.ROLE_NAME_MAX_LENGTH,
            message = "Role name must be between 3 and 100 characters."
    )
    private String name;

    @Schema(
            description = "Unique role code",
            example = "SALES_MANAGER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Role code is required.")
    @Size(
            min = RoleConstants.ROLE_CODE_MIN_LENGTH,
            max = RoleConstants.ROLE_CODE_MAX_LENGTH,
            message = "Role code must be between 2 and 30 characters."
    )
    private String code;

    @Schema(
            description = "Role description",
            example = "Responsible for managing Team Leaders and Sales Executives."
    )
    @Size(
            max = RoleConstants.DESCRIPTION_MAX_LENGTH,
            message = "Description cannot exceed 500 characters."
    )
    private String description;

    @Schema(
            description = "Additional remarks",
            example = "Updated by Super Administrator."
    )
    @Size(
            max = RoleConstants.DESCRIPTION_MAX_LENGTH,
            message = "Remarks cannot exceed 500 characters."
    )
    private String remarks;

}