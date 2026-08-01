package com.blueant_crm_erp.bootstrap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =============================================================================
 * Bootstrap Request
 * =============================================================================
 *
 * Request DTO used to trigger database bootstrap manually.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Select bootstrap mode
 * • Allow force bootstrap
 * • Allow module-specific bootstrap
 *
 * Notes
 * -----------------------------------------------------------------------------
 * This request is intended for administrative APIs only.
 * It must never be exposed to normal users.
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
@Schema(description = "Bootstrap Request")
public class BootstrapRequest {

    /**
     * Force bootstrap even if master data already exists.
     */
    @Builder.Default
    @NotNull
    @Schema(
            description = "Force database bootstrap",
            example = "false"
    )
    private Boolean force = Boolean.FALSE;

    /**
     * Bootstrap master data.
     */
    @Builder.Default
    @NotNull
    @Schema(
            description = "Seed master data",
            example = "true"
    )
    private Boolean masterData = Boolean.TRUE;

    /**
     * Bootstrap security data.
     */
    @Builder.Default
    @NotNull
    @Schema(
            description = "Seed roles, permissions and role-permission mappings",
            example = "true"
    )
    private Boolean securityData = Boolean.TRUE;

    /**
     * Bootstrap default system users.
     */
    @Builder.Default
    @NotNull
    @Schema(
            description = "Seed default users",
            example = "true"
    )
    private Boolean users = Boolean.TRUE;

}