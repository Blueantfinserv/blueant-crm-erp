package com.blueant_crm_erp.permission.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Permission Repository
 * =============================================================================
 *
 * Repository for Permission Entity.
 *
 * Responsibilities:
 * ------------------------------------------------------------------
 * • CRUD Operations
 * • Dynamic Search (Specification)
 * • Soft Delete Support
 * • Duplicate Validation
 * • Dropdown APIs
 * • Sorting
 * • Statistics
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 * =============================================================================
 */
@Repository
public interface PermissionRepository
        extends JpaRepository<Permission, Long>,
        JpaSpecificationExecutor<Permission> {

    // =========================================================================
    // Find By Id
    // =========================================================================

    Optional<Permission> findByIdAndDeletedFalse(Long id);

    Optional<Permission> findByIdAndDeletedTrue(Long id);

    // =========================================================================
    // Find By Code / Name
    // =========================================================================

    Optional<Permission> findByCodeIgnoreCase(String code);

    Optional<Permission> findByNameIgnoreCase(String name);

    Optional<Permission> findByCodeIgnoreCaseAndDeletedFalse(String code);

    Optional<Permission> findByNameIgnoreCaseAndDeletedFalse(String name);

    // =========================================================================
    // Exists
    // =========================================================================

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    // =========================================================================
    // Listing
    // =========================================================================

    /**
     * All Active Permissions
     */
    List<Permission> findAllByDeletedFalse();

    /**
     * All Active Permissions Ordered By Name
     */
    List<Permission> findAllByDeletedFalseOrderByNameAsc();

    /**
     * All Active Permissions Ordered By Display Order
     */
    List<Permission> findAllByDeletedFalseOrderByDisplayOrderAsc();

    /**
     * All Permissions
     */
    List<Permission> findAllByOrderByDisplayOrderAsc();

    /**
     * Module Wise Permissions
     */
    List<Permission> findAllByModuleIgnoreCaseAndDeletedFalseOrderByDisplayOrderAsc(
            String module
    );

    /**
     * Status Wise Permissions
     */
    List<Permission> findAllByStatusAndDeletedFalse(Status status);

    /**
     * Status Wise Permissions Ordered
     */
    List<Permission> findAllByStatusAndDeletedFalseOrderByDisplayOrderAsc(
            Status status
    );

    /**
     * System Permissions
     */
    List<Permission> findAllBySystemPermissionTrueAndDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Custom Permissions
     */
    List<Permission> findAllBySystemPermissionFalseAndDeletedFalseOrderByDisplayOrderAsc();

    // =========================================================================
    // Count
    // =========================================================================

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(Status status);

    long countByModuleIgnoreCaseAndDeletedFalse(String module);

}