package com.blueant_crm_erp.bootstrap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * Bootstrap Execution Report
 * =============================================================================
 *
 * Detailed execution report for the bootstrap process.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootstrapExecutionReport {

    private int totalModules;
    private int executedModules;
    private int successfulModules;
    private int failedModules;
    private int skippedModules;

    private int totalRecordsInserted;
    private int totalRecordsSkipped;
    private int totalRecordsFailed;

    private long executionTimeMs;

    private Map<String, String> failureDetails;
    private List<String> warningDetails;
    private List<String> completedModules;

    private String databaseVersion;
    private String javaVersion;
    private String springBootVersion;
    private String applicationVersion;

    private LocalDateTime timestamp;

}
