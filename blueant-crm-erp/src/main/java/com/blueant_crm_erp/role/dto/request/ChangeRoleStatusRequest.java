package com.blueant_crm_erp.role.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
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
 * ============================================================================
 * Change Role Status Request
 * ============================================================================
 *
 * Request DTO used to activate or deactivate an existing role.
 *
 * API:
 * PATCH /api/v1/roles/{roleId}/status
 *
 * Example Request:
 *
 * {
 *     "status": "ACTIVE",
 *     "remarks": "Role activated by Super Admin."
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
public class ChangeRoleStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * New status of the role.
     */
    @NotNull(message = "Role status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Reason for changing role status.
     */
    @NotBlank(message = "Remarks are required.")
    @Size(max = 500, message = "Remarks cannot exceed 500 characters.")
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}