package com.blueant_crm_erp.bootstrap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =============================================================================
 * Reset Bootstrap Request
 * =============================================================================
 *
 * Request DTO for resetting bootstrap data.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Confirm reset operation
 * • Allow force reset
 * • Select reset scope
 *
 * Notes
 * -----------------------------------------------------------------------------
 * • Intended for Super Admin only.
 * • Must never be exposed to normal users.
 * • Reset operations should always be audited.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reset Bootstrap Request")
public class ResetBootstrapRequest {

    /**
     * Confirmation keyword.
     *
     * Expected Value:
     * RESET
     */
    @NotBlank(message = "Confirmation is required.")
    @Schema(
            description = "Confirmation keyword",
            example = "RESET",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmation;

    /**
     * Force reset even if bootstrap has already been executed.
     */
    @Builder.Default
    @NotNull(message = "Force flag is required.")
    @Schema(
            description = "Force reset",
            example = "false"
    )
    private Boolean force = Boolean.FALSE;

    /**
     * Include master data.
     */
    @Builder.Default
    @NotNull(message = "Master data flag is required.")
    @Schema(
            description = "Reset master data",
            example = "true"
    )
    private Boolean masterData = Boolean.TRUE;

    /**
     * Include security data.
     */
    @Builder.Default
    @NotNull(message = "Security data flag is required.")
    @Schema(
            description = "Reset roles, permissions and mappings",
            example = "true"
    )
    private Boolean securityData = Boolean.TRUE;

    /**
     * Include default users.
     */
    @Builder.Default
    @NotNull(message = "Users flag is required.")
    @Schema(
            description = "Reset default users",
            example = "true"
    )
    private Boolean users = Boolean.TRUE;

}