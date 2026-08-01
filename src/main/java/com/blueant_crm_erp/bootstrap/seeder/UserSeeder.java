package com.blueant_crm_erp.bootstrap.seeder;

import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;

/**
 * =============================================================================
 * User Seeder
 * =============================================================================
 *
 * Seeds default system users during initial database bootstrap.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create Default Super Admin
 * • Create Default Admin
 * • Create Demo Users (Optional)
 * • Skip Existing Users
 * • Maintain Idempotent Seeding
 *
 * Notes
 * -----------------------------------------------------------------------------
 * • Executes only during bootstrap.
 * • Must never create duplicate users.
 * • Passwords must always be BCrypt encoded.
 * * Depends on:
 *   - Department
 *   - Designation
 *   - Team
 *   - Role
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface UserSeeder {

    /**
     * Seeds all default system users.
     */
    SeederResult seed();

}