package com.blueant_crm_erp.user.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.dto.request.ChangeDesignationStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDesignationRequest;
import com.blueant_crm_erp.user.dto.request.DesignationSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDesignationRequest;
import com.blueant_crm_erp.user.dto.response.DesignationDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DesignationResponse;
import com.blueant_crm_erp.user.dto.response.DesignationSummaryResponse;

import java.util.List;

/**
 * =============================================================================
 * Designation Service
 * =============================================================================
 *
 * Business operations for Designation Management.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Rohit (Business Head)
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create Designation
 * • Update Designation
 * • Delete Designation
 * • Restore Designation
 * • Change Designation Status
 * • Search Designations
 * • Designation Dropdown
 * • Count Designations
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface DesignationService {

    /**
     * Create Designation.
     */
    DesignationResponse createDesignation(
            CreateDesignationRequest request
    );

    /**
     * Update Designation.
     */
    DesignationResponse updateDesignation(
            Long designationId,
            UpdateDesignationRequest request
    );

    /**
     * Soft Delete Designation.
     */
    void deleteDesignation(
            Long designationId
    );

    /**
     * Restore Deleted Designation.
     */
    void restoreDesignation(
            Long designationId
    );

    /**
     * Change Designation Status.
     */
    DesignationResponse changeDesignationStatus(
            Long designationId,
            ChangeDesignationStatusRequest request
    );

    /**
     * Get Designation By Id.
     */
    DesignationResponse getDesignationById(
            Long designationId
    );

    /**
     * Get Designation By Name.
     */
    DesignationResponse getDesignationByName(
            String designationName
    );

    /**
     * Get All Active Designations.
     */
    List<DesignationSummaryResponse> getAllDesignations();

    /**
     * Get All Designations Including Deleted.
     */
    List<DesignationSummaryResponse> getAllDesignationsIncludingDeleted();

    /**
     * Search Designations.
     */
    PageResponse<DesignationSummaryResponse> searchDesignations(
            DesignationSearchRequest request
    );

    /**
     * Designation Dropdown.
     */
    List<DesignationDropdownResponse> getDesignationDropdown();

    /**
     * Check Designation Exists.
     */
    boolean existsById(
            Long designationId
    );

    /**
     * Check Designation Name Exists.
     */
    boolean existsByName(
            String designationName
    );

    /**
     * Count Total Designations.
     */
    long countDesignations();

    /**
     * Count Active Designations.
     */
    long countActiveDesignations();

}