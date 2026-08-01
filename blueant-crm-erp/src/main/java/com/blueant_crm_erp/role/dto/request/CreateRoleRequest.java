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
 * Create Role Request
 * ============================================================================
 *
 * Request DTO used to create a new Role.
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
        name = "CreateRoleRequest",
        description = "Request payload used to create a new role."
)
public class CreateRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(
            description = "Role name",
            example = "Administrator",
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
            example = "ADMIN",
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
            example = "System administrator with full access."
    )
    @Size(
            max = RoleConstants.DESCRIPTION_MAX_LENGTH,
            message = "Description must not exceed 500 characters."
    )
    private String description;

    @Schema(
            description = "Display order of the role",
            example = "1"
    )
    private Integer displayOrder;

    @Schema(
            description = "Indicates whether this is the default role",
            example = "false",
            defaultValue = "false"
    )
    @Builder.Default
    private Boolean defaultRole = Boolean.FALSE;

    @Schema(
            description = "Additional remarks",
            example = "Created during initial system setup."
    )
    @Size(
            max = RoleConstants.DESCRIPTION_MAX_LENGTH,
            message = "Remarks must not exceed 500 characters."
    )
    private String remarks;

}