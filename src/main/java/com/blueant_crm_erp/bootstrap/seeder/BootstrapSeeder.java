package com.blueant_crm_erp.bootstrap.seeder;

import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;

/**
 * =============================================================================
 * Bootstrap Seeder Interface
 * =============================================================================
 *
 * Common contract for all database seeders.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface BootstrapSeeder {

    /**
     * Execute the seeder logic.
     *
     * @return result of execution containing metrics
     */
    SeederResult seed();

    /**
     * Name of the module being seeded.
     *
     * @return module name
     */
    String module();

    /**
     * Execution order. Lower numbers execute first.
     * Independent seeders with the same order can execute in parallel.
     *
     * @return execution order
     */
    int order();

    /**
     * Whether this seeder is currently enabled.
     *
     * @return true if enabled
     */
    boolean enabled();

}
