package com.blueant_crm_erp.bootstrap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * Bootstrap Status Response
 * =============================================================================
 *
 * Represents the current bootstrap status of the application.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Bootstrap Status
 * • Database Initialization Status
 * • Seeded Modules
 * • Pending Modules
 * • Default Admin Status
 * • Last Bootstrap Information
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
@Schema(description = "Bootstrap Status Response")
public class BootstrapStatusResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether bootstrap has completed.
     */
    @Schema(
            description = "Bootstrap completed",
            example = "true"
    )
    private Boolean bootstrapCompleted;

    /**
     * Indicates whether the database has been initialized.
     */
    @Schema(
            description = "Database initialized",
            example = "true"
    )
    private Boolean databaseInitialized;

    /**
     * Indicates whether Super Admin exists.
     */
    @Schema(
            description = "Default Super Admin exists",
            example = "true"
    )
    private Boolean superAdminCreated;

    /**
     * Total configured bootstrap modules.
     */
    @Schema(
            description = "Total bootstrap modules",
            example = "7"
    )
    private Integer totalModules;

    /**
     * Successfully completed modules.
     */
    @Schema(
            description = "Completed modules"
    )
    private List<String> completedModules;

    /**
     * Modules still pending.
     */
    @Schema(
            description = "Pending modules"
    )
    private List<String> pendingModules;

    /**
     * Last bootstrap execution time.
     */
    @Schema(
            description = "Last bootstrap execution time"
    )
    private LocalDateTime lastBootstrapTime;

    /**
     * Current bootstrap status message.
     */
    @Schema(
            description = "Bootstrap status message",
            example = "Database bootstrap completed successfully."
    )
    private String message;

}