package com.blueant_crm_erp.permission.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Change Permission Status Request
 * =============================================================================
 *
 * Request DTO used to change the status of an existing Permission.
 *
 * Supported Status Values:
 * • ACTIVE
 * • INACTIVE
 * • SUSPENDED
 * • ARCHIVED
 *
 * This DTO is used by the Permission Status API to activate,
 * deactivate, suspend or archive a permission.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Example Request:
 *
 * {
 *     "status": "ACTIVE"
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
public class ChangePermissionStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Permission Status.
     */
    @NotNull(message = "Permission status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

}