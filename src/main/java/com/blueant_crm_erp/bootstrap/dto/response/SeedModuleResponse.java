package com.blueant_crm_erp.bootstrap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * Seed Module Response
 * =============================================================================
 *
 * Represents the execution result of module-wise database seeding.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Executed Modules
 * • Skipped Modules
 * • Total Records Seeded
 * • Execution Time
 * • Execution Status
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
@Schema(description = "Seed Module Response")
public class SeedModuleResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether module seeding completed successfully.
     */
    @Schema(
            description = "Module seeding status",
            example = "true"
    )
    private Boolean success;

    /**
     * Execution message.
     */
    @Schema(
            description = "Execution message",
            example = "Selected modules seeded successfully."
    )
    private String message;

    /**
     * Successfully seeded modules.
     */
    @Schema(
            description = "Executed bootstrap modules"
    )
    private List<String> seededModules;

    /**
     * Skipped modules.
     */
    @Schema(
            description = "Skipped bootstrap modules"
    )
    private List<String> skippedModules;

    /**
     * Total records inserted.
     */
    @Schema(
            description = "Total inserted records",
            example = "148"
    )
    private Integer totalRecordsInserted;

    /**
     * Total execution time in milliseconds.
     */
    @Schema(
            description = "Execution time in milliseconds",
            example = "1240"
    )
    private Long executionTime;

    /**
     * Execution timestamp.
     */
    @Schema(
            description = "Execution timestamp"
    )
    private LocalDateTime executedAt;

}