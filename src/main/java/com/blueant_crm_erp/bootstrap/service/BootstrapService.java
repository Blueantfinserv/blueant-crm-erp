package com.blueant_crm_erp.bootstrap.service;

/**
 * =============================================================================
 * Bootstrap Service
 * =============================================================================
 *
 * Responsible for initializing default master data
 * required by BlueAnt CRM ERP.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Bootstrap Departments
 * • Bootstrap Designations
 * • Bootstrap Teams
 * • Bootstrap Roles
 * • Bootstrap Permissions
 * • Bootstrap Role-Permission Mapping
 * • Bootstrap Super Admin User
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
import com.blueant_crm_erp.bootstrap.dto.request.BootstrapRequest;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapExecutionReport;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapStatusResponse;

public interface BootstrapService {

    /**
     * Executes complete database bootstrap.
     */
    void bootstrap();

    /**
     * Executes complete database bootstrap with request.
     */
    BootstrapExecutionReport bootstrap(BootstrapRequest request);

    /**
     * Retrieves the current bootstrap status.
     */
    BootstrapStatusResponse getBootstrapStatus();

}