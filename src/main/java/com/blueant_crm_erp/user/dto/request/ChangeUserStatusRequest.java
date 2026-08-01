package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
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
 * Change User Status Request
 * =============================================================================
 *
 * Request DTO used to Activate / Deactivate a User.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Supported Status
 * -----------------------------------------------------------------------------
 * ACTIVE
 * INACTIVE
 *
 * Note
 * -----------------------------------------------------------------------------
 * This API is used by:
 * • Super Admin
 * • Admin
 * • Business Head
 * • HR
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head (Rohit)
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
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
public class ChangeUserStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Status.
     */
    @NotNull(message = "User status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Reason for status change.
     */
    @Size(
            max = 500,
            message = "Reason must not exceed 500 characters."
    )
    @Schema(description = "Reason", example = "Example Reason", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

}