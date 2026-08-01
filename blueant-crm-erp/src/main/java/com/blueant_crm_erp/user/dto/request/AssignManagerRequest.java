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
 * Assign Manager Request
 * =============================================================================
 *
 * Request DTO used to assign or change a Reporting Manager
 * for an existing User.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
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
 * Used By
 * -----------------------------------------------------------------------------
 * • Super Admin
 * • Admin
 * • Business Head
 * • HR
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
public class AssignManagerRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Reporting Manager Id.
     */
    @NotNull(message = "Reporting manager is required.")
    @Schema(description = "Reporting Manager Id", example = "Example Reporting Manager Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportingManagerId;

    /**
     * Effective From.
     *
     * Example:
     * Immediate
     * Promotion Date
     * Transfer Date
     */
    @Size(
            max = 100,
            message = "Effective from must not exceed 100 characters."
    )
    @Schema(description = "Effective From", example = "Example Effective From", requiredMode = Schema.RequiredMode.REQUIRED)
    private String effectiveFrom;

    /**
     * Reason for manager assignment.
     */
    @Size(
            max = 500,
            message = "Reason must not exceed 500 characters."
    )
    @Schema(description = "Reason", example = "Example Reason", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

}