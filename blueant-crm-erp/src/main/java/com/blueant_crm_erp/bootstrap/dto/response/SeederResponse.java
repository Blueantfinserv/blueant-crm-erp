package com.blueant_crm_erp.bootstrap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Seeder Response
 * =============================================================================
 *
 * Represents the execution result of an individual Seeder.
 *
 * Used internally by BootstrapService to build the final
 * BootstrapResponse.
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
@Schema(description = "Seeder Execution Response")
public class SeederResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Seeder Name.
     */
    @Schema(example = "RoleSeeder")
    private String seederName;

    /**
     * Indicates whether execution succeeded.
     */
    @Schema(example = "true")
    private Boolean success;

    /**
     * Total inserted records.
     */
    @Schema(example = "5")
    private Integer insertedRecords;

    /**
     * Total skipped records.
     */
    @Schema(example = "0")
    private Integer skippedRecords;

    /**
     * Execution message.
     */
    @Schema(example = "Roles seeded successfully.")
    private String message;

}