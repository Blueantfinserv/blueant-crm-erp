package com.blueant_crm_erp.bootstrap.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * =============================================================================
 * Seed Module Request
 * =============================================================================
 *
 * Request DTO for module-wise database seeding.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Seed selected bootstrap modules
 * • Support partial database initialization
 * • Prevent unnecessary full bootstrap execution
 *
 * Notes
 * -----------------------------------------------------------------------------
 * • Intended for Super Admin / DevOps use only.
 * • Multiple modules can be seeded in a single request.
 * • BootstrapService is responsible for validating dependencies.
 *
 * Example
 * -----------------------------------------------------------------------------
 * {
 *   "modules": [
 *      "DEPARTMENT",
 *      "ROLE",
 *      "PERMISSION"
 *   ],
 *   "force": false
 * }
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
@Schema(description = "Seed Module Request")
public class SeedModuleRequest {

    /**
     * Modules to seed.
     */
    @NotEmpty(message = "At least one module must be selected.")
    @Builder.Default
    @Schema(
            description = "Modules to seed",
            example = "[\"DEPARTMENT\",\"ROLE\",\"PERMISSION\"]"
    )
    private Set<String> modules = new LinkedHashSet<>();

    /**
     * Force seeding even if data already exists.
     */
    @Builder.Default
    @Schema(
            description = "Force module seeding",
            example = "false"
    )
    private Boolean force = Boolean.FALSE;

}