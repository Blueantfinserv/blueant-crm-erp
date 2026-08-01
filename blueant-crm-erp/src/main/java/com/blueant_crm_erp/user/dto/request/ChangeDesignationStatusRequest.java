package com.blueant_crm_erp.user.dto.request;

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
 * =============================================================================
 * Change Designation Status Request
 * =============================================================================
 *
 * Request DTO used to activate or deactivate an existing Designation.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Activate Designation
 * • Deactivate Designation
 * • Maintain Audit Trail
 *
 * Supported Status
 * -----------------------------------------------------------------------------
 * • ACTIVE
 * • INACTIVE
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Super Admin
 * • Admin
 * • HR
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
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
public class ChangeDesignationStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * New Designation Status.
     */
    @NotNull(message = "Designation status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Reason for changing status.
     */
    @NotBlank(message = "Reason is required.")
    @Size(
            max = 500,
            message = "Reason must not exceed 500 characters."
    )
    @Schema(description = "Reason", example = "Example Reason", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

}