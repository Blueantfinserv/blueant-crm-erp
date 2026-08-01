package com.blueant_crm_erp.role.service;

import com.blueant_crm_erp.role.dto.request.AssignPermissionRequest;
import com.blueant_crm_erp.role.dto.response.RolePermissionResponse;

import java.util.List;

/**
 * =============================================================================
 * Role Permission Service
 * =============================================================================
 *
 * Business contract for Role Permission Management.
 *
 * Responsibilities:
 * - Assign Permissions to Role
 * - Remove Permissions from Role
 * - Get Permissions by Role
 * - Check Permission Assignment
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface RolePermissionService {

    /**
     * Assign permissions to a role.
     *
     * @param request permission assignment request
     * @return assigned permissions
     */
    List<RolePermissionResponse> assignPermissions(
            AssignPermissionRequest request);

    /**
     * Remove permissions from a role.
     *
     * @param request permission removal request
     */
    void removePermissions(
            AssignPermissionRequest request);

    /**
     * Get all permissions assigned to a role.
     *
     * @param roleId role identifier
     * @return permission list
     */
    List<RolePermissionResponse> getPermissionsByRoleId(
            Long roleId);

    /**
     * Check whether a permission is assigned to a role.
     *
     * @param roleId role identifier
     * @param permissionId permission identifier
     * @return true if assigned
     */
    boolean hasPermission(
            Long roleId,
            Long permissionId);

}