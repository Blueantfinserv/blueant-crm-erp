package com.blueant_crm_erp.bootstrap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Seeder Result
 * =============================================================================
 *
 * Encapsulates the execution metrics of a specific Bootstrap Seeder.
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
public class SeederResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Name of the module seeded (e.g., "Departments", "Roles").
     */
    private String moduleName;

    /**
     * Total records that were expected to be processed.
     */
    private int totalCount;

    /**
     * Total records successfully inserted.
     */
    private int insertedCount;

    /**
     * Total records skipped due to idempotency checks.
     */
    private int skippedCount;

    /**
     * Execution time in milliseconds.
     */
    private long executionTime;

    /**
     * Status of the seeder execution (e.g., "SUCCESS", "FAILED").
     */
    private String status;

}
