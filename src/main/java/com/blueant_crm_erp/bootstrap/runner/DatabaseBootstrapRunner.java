package com.blueant_crm_erp.bootstrap.runner;

import com.blueant_crm_erp.bootstrap.service.BootstrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * Database Bootstrap Runner
 * =============================================================================
 *
 * Executes database bootstrap during application startup.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Execute database bootstrap
 * • Create master data
 * • Create default roles
 * • Create permissions
 * • Create role-permission mappings
 * • Create default super admin
 * • Prevent duplicate bootstrapping
 *
 * Execution Flow
 * -----------------------------------------------------------------------------
 * Application Start
 *          │
 *          ▼
 * DatabaseBootstrapRunner
 *          │
 *          ▼
 * BootstrapService
 *          │
 *          ▼
 * Department Seeder
 *          ▼
 * Designation Seeder
 *          ▼
 * Team Seeder
 *          ▼
 * Role Seeder
 *          ▼
 * Permission Seeder
 *          ▼
 * RolePermission Seeder
 *          ▼
 * Super Admin Seeder
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseBootstrapRunner implements CommandLineRunner {

    /**
     * Bootstrap Service.
     */
    private final BootstrapService bootstrapService;

    /**
     * Enable / Disable bootstrap.
     *
     * application.yml
     *
     * blueant:
     *   bootstrap:
     *      enabled: true
     */
    @Value("${blueant.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    /**
     * Executes database bootstrap after application startup.
     */
    @Override
    public void run(String... args) {

        if (!bootstrapEnabled) {

            log.info("====================================================");
            log.info("Database Bootstrap is disabled.");
            log.info("====================================================");

            return;
        }

        log.info("====================================================");
        log.info("Starting BlueAnt CRM ERP Database Bootstrap...");
        log.info("====================================================");

        long startTime = System.currentTimeMillis();

        try {

            bootstrapService.bootstrap();

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("====================================================");
            log.info("Database Bootstrap Completed Successfully.");
            log.info("Execution Time : {} ms", executionTime);
            log.info("====================================================");

        } catch (Exception exception) {

            log.error("====================================================");
            log.error("Database Bootstrap Failed.");
            log.error("Reason : {}", exception.getMessage(), exception);
            log.error("====================================================");

            throw new IllegalStateException(
                    "Failed to initialize bootstrap data.",
                    exception
            );
        }

    }

}