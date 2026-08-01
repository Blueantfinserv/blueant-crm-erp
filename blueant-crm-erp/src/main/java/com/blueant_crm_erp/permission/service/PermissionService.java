package com.blueant_crm_erp.permission.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.permission.dto.request.ChangePermissionStatusRequest;
import com.blueant_crm_erp.permission.dto.request.CreatePermissionRequest;
import com.blueant_crm_erp.permission.dto.request.PermissionSearchRequest;
import com.blueant_crm_erp.permission.dto.request.UpdatePermissionRequest;
import com.blueant_crm_erp.permission.dto.response.PermissionDropdownResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionSummaryResponse;

import java.util.List;

/**
 * =============================================================================
 * Permission Service
 * =============================================================================
 *
 * Business contract for Permission Management.
 *
 * Responsibilities:
 * • Create Permission
 * • Update Permission
 * • Delete Permission
 * • Restore Permission
 * • Change Permission Status
 * • Get Permission Details
 * • Search Permissions
 * • Dropdown APIs
 * • Validation APIs
 * • Statistics APIs
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
public interface PermissionService {

    /**
     * Create a new Permission.
     *
     * @param request create permission request
     * @return created permission
     */
    PermissionResponse createPermission(
            CreatePermissionRequest request
    );

    /**
     * Update existing Permission.
     *
     * @param permissionId permission id
     * @param request update request
     * @return updated permission
     */
    PermissionResponse updatePermission(
            Long permissionId,
            UpdatePermissionRequest request
    );

    /**
     * Soft delete Permission.
     *
     * @param permissionId permission id
     */
    void deletePermission(
            Long permissionId
    );

    /**
     * Restore deleted Permission.
     *
     * @param permissionId permission id
     */
    void restorePermission(
            Long permissionId
    );

    /**
     * Change Permission Status.
     *
     * @param permissionId permission id
     * @param request status request
     * @return updated permission
     */
    PermissionResponse changePermissionStatus(
            Long permissionId,
            ChangePermissionStatusRequest request
    );

    /**
     * Get Permission by Id.
     *
     * @param permissionId permission id
     * @return permission details
     */
    PermissionResponse getPermissionById(
            Long permissionId
    );

    /**
     * Get Permission by Code.
     *
     * @param code permission code
     * @return permission details
     */
    PermissionResponse getPermissionByCode(
            String code
    );

    /**
     * Get all active Permissions.
     *
     * @return permission list
     */
    List<PermissionSummaryResponse> getAllPermissions();

    /**
     * Get all Permissions including deleted.
     *
     * @return permission list
     */
    List<PermissionSummaryResponse> getAllPermissionsIncludingDeleted();

    /**
     * Search Permissions.
     *
     * @param request search request
     * @return paginated permissions
     */
    PageResponse<PermissionSummaryResponse> searchPermissions(
            PermissionSearchRequest request
    );

    /**
     * Permission Dropdown.
     *
     * @return dropdown list
     */
    List<PermissionDropdownResponse> getPermissionDropdown();

    /**
     * Check Permission exists.
     *
     * @param permissionId permission id
     * @return true if exists
     */
    boolean existsById(
            Long permissionId
    );

    /**
     * Check Permission Code exists.
     *
     * @param code permission code
     * @return true if exists
     */
    boolean existsByCode(
            String code
    );

    /**
     * Check Permission Name exists.
     *
     * @param name permission name
     * @return true if exists
     */
    boolean existsByName(
            String name
    );

    /**
     * Check whether Permission is assigned
     * to any Role.
     *
     * @param permissionId permission id
     * @return true if assigned
     */
    boolean isAssignedToAnyRole(
            Long permissionId
    );

    /**
     * Total Permission Count.
     *
     * @return total permissions
     */
    long countPermissions();

    /**
     * Total Active Permission Count.
     *
     * @return active permission count
     */
    long countActivePermissions();

}