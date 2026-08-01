package com.blueant_crm_erp.permission.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
 * Update Permission Request
 * =============================================================================
 *
 * Request DTO used to update an existing Permission.
 *
 * This DTO contains all editable fields of a Permission.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Responsibilities:
 * • Validate update request
 * • Transfer updated permission data
 * • Prevent invalid input
 *
 * Example Request:
 *
 * {
 *   "name": "Update Lead",
 *   "code": "UPDATE_LEAD",
 *   "module": "SALES",
 *   "description": "Allows user to update leads.",
 *   "displayOrder": 2,
 *   "systemPermission": false,
 *   "status": "ACTIVE",
 *   "remarks": "Updated permission"
 * }
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
public class UpdatePermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Permission Name.
     */
    @NotBlank(message = "Permission name is required.")
    @Size(
            min = 3,
            max = 100,
            message = "Permission name must be between 3 and 100 characters."
    )
    @Schema(description = "Name", example = "Example Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Unique Permission Code.
     */
    @NotBlank(message = "Permission code is required.")
    @Size(
            min = 3,
            max = 100,
            message = "Permission code must be between 3 and 100 characters."
    )
    @Schema(description = "Code", example = "Example Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * Module Name.
     *
     * Examples:
     * SALES
     * CRM
     * USER
     * ROLE
     * HR
     * REPORT
     * DASHBOARD
     */
    @NotBlank(message = "Module name is required.")
    @Size(
            min = 2,
            max = 100,
            message = "Module name must be between 2 and 100 characters."
    )
    @Schema(description = "Module", example = "Example Module", requiredMode = Schema.RequiredMode.REQUIRED)
    private String module;

    /**
     * Permission Description.
     */
    @Size(
            max = 500,
            message = "Description must not exceed 500 characters."
    )
    @Schema(description = "Description", example = "Example Description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * Display Order.
     */
    @NotNull(message = "Display order is required.")
    @Min(
            value = 1,
            message = "Display order must be greater than zero."
    )
    @Max(
            value = 9999,
            message = "Display order must not exceed 9999."
    )
    @Schema(description = "Display Order", example = "Example Display Order", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    /**
     * Indicates whether this is a System Permission.
     *
     * System permissions cannot be modified by
     * normal administrators.
     */
    @NotNull(message = "System permission flag is required.")
    @Schema(description = "System Permission", example = "Example System Permission", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean systemPermission;

    /**
     * Permission Status.
     */
    @NotNull(message = "Permission status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Additional Remarks.
     */
    @Size(
            max = 500,
            message = "Remarks must not exceed 500 characters."
    )
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}