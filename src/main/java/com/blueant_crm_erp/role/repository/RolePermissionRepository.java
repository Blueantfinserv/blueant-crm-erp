package com.blueant_crm_erp.role.repository;

import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Role Permission Repository
 * =============================================================================
 *
 * Repository for Role-Permission Mapping.
 *
 * Responsibilities
 * ---------------------------------------------------------------------------
 * • Assign Permission to Role
 * • Remove Permission from Role
 * • Check Existing Mapping
 * • Check Permission Assignment
 * • Fetch Permissions by Role
 * • Fetch Roles by Permission
 * • Delete Role-Permission Mapping
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Repository
public interface RolePermissionRepository
        extends JpaRepository<RolePermission, Long> {

    // =========================================================================
    // Find By Role / Permission
    // =========================================================================

    /**
     * Get all mappings of a Role.
     *
     * @param role Role Entity
     * @return List of RolePermission
     */
    List<RolePermission> findAllByRole(Role role);

    /**
     * Get all mappings of a Permission.
     *
     * @param permission Permission Entity
     * @return List of RolePermission
     */
    List<RolePermission> findAllByPermission(Permission permission);

    /**
     * Get all mappings by Role Id.
     *
     * @param roleId Role Id
     * @return List of RolePermission
     */
    List<RolePermission> findAllByRoleId(Long roleId);

    /**
     * Get all mappings by Permission Id.
     *
     * @param permissionId Permission Id
     * @return List of RolePermission
     */
    List<RolePermission> findAllByPermissionId(Long permissionId);

    // =========================================================================
    // Find Specific Mapping
    // =========================================================================

    /**
     * Find Role-Permission mapping.
     *
     * @param roleId Role Id
     * @param permissionId Permission Id
     * @return Optional RolePermission
     */
    Optional<RolePermission> findByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId
    );

    // =========================================================================
    // Exists
    // =========================================================================

    /**
     * Check whether a Role already has a Permission.
     *
     * @param roleId Role Id
     * @param permissionId Permission Id
     * @return true if mapping exists
     */
    boolean existsByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId
    );

    /**
     * Check whether a Permission is assigned
     * to at least one Role.
     *
     * Used by PermissionService before deleting
     * a Permission.
     *
     * @param permissionId Permission Id
     * @return true if assigned
     */
    boolean existsByPermissionId(Long permissionId);

    /**
     * Check whether a Role has any Permission.
     *
     * @param roleId Role Id
     * @return true if mappings exist
     */
    boolean existsByRoleId(Long roleId);

    // =========================================================================
    // Delete
    // =========================================================================

    /**
     * Delete a specific mapping.
     *
     * @param roleId Role Id
     * @param permissionId Permission Id
     */
    void deleteByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId
    );

    /**
     * Delete all mappings of a Role.
     *
     * @param roleId Role Id
     */
    void deleteAllByRoleId(Long roleId);

    /**
     * Delete all mappings of a Permission.
     *
     * @param permissionId Permission Id
     */
    void deleteAllByPermissionId(Long permissionId);

}