package com.blueant_crm_erp.permission.dto.request;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ============================================================================
 * Create Permission Request
 * ============================================================================
 *
 * Request DTO used to create a new Permission in the RBAC module.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
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
        name = "CreatePermissionRequest",
        description = "Request payload used to create a new permission."
)
public class CreatePermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(
            description = "Permission name",
            example = "Create Lead",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Permission name is required.")
    @Size(
            min = PermissionConstants.NAME_MIN_LENGTH,
            max = PermissionConstants.NAME_MAX_LENGTH,
            message = "Permission name must be between 3 and 100 characters."
    )
    private String name;

    @Schema(
            description = "Unique permission code",
            example = "CREATE_LEAD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Permission code is required.")
    @Size(
            min = PermissionConstants.CODE_MIN_LENGTH,
            max = PermissionConstants.CODE_MAX_LENGTH,
            message = "Permission code must be between 3 and 100 characters."
    )
    private String code;

    @Schema(
            description = "Business module to which this permission belongs",
            example = "SALES",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Module name is required.")
    @Size(
            min = 2,
            max = PermissionConstants.MODULE_MAX_LENGTH,
            message = "Module name must be between 2 and 100 characters."
    )
    private String module;

    @Schema(
            description = "Permission description",
            example = "Allows users to create new sales leads."
    )
    @Size(
            max = PermissionConstants.DESCRIPTION_MAX_LENGTH,
            message = "Description must not exceed 500 characters."
    )
    private String description;

    @Schema(
            description = "Display order of the permission",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Display order is required.")
    @Min(
            value = PermissionConstants.DISPLAY_ORDER_MIN,
            message = "Display order must be greater than zero."
    )
    @Max(
            value = PermissionConstants.DISPLAY_ORDER_MAX,
            message = "Display order must not exceed 9999."
    )
    private Integer displayOrder;

    @Schema(
            description = "Whether this is a system permission",
            example = "false",
            defaultValue = "false"
    )
    @Builder.Default
    @NotNull(message = "System permission flag is required.")
    private Boolean systemPermission = Boolean.FALSE;

    @Schema(
            description = "Permission status",
            example = "ACTIVE",
            defaultValue = "ACTIVE"
    )
    @Builder.Default
    @NotNull(message = "Permission status is required.")
    private Status status = Status.ACTIVE;

    @Schema(
            description = "Additional remarks",
            example = "Default permission created during system initialization."
    )
    @Size(
            max = PermissionConstants.REMARKS_MAX_LENGTH,
            message = "Remarks must not exceed 500 characters."
    )
    private String remarks;

}