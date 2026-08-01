package com.blueant_crm_erp.user.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Designation Repository
 * =============================================================================
 *
 * Repository for Designation Entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • CRUD Operations
 * • Dynamic Search (Specification)
 * • Soft Delete Support
 * • Duplicate Validation
 * • Department Wise Designations
 * • Hierarchy Level Queries
 * • Dropdown APIs
 * • Dashboard Counts
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Level 1 → Business Head
 * Level 2 → Sales Manager
 * Level 3 → Team Leader
 * Level 4 → Sales Person
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Repository
public interface DesignationRepository extends
        JpaRepository<Designation, Long>,
        JpaSpecificationExecutor<Designation> {

    // =========================================================================
    // Find By Id
    // =========================================================================

    Optional<Designation> findByIdAndDeletedFalse(Long id);

    Optional<Designation> findByIdAndDeletedTrue(Long id);

    // =========================================================================
    // Find By Name
    // =========================================================================

    Optional<Designation> findByNameIgnoreCase(String name);

    Optional<Designation> findByNameIgnoreCaseAndDeletedFalse(String name);

    // =========================================================================
    // Find By Code
    // =========================================================================

    Optional<Designation> findByCodeIgnoreCase(String code);

    Optional<Designation> findByCodeIgnoreCaseAndDeletedFalse(String code);

    // =========================================================================
    // Exists Validation
    // =========================================================================

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    // =========================================================================
    // Listing
    // =========================================================================

    List<Designation> findAllByDeletedFalseOrderByHierarchyLevelAscDisplayOrderAsc();

    List<Designation> findAllByOrderByHierarchyLevelAscDisplayOrderAsc();

    List<Designation> findAllByStatusAndDeletedFalseOrderByHierarchyLevelAscDisplayOrderAsc(
            Status status
    );

    List<Designation> findAllByDepartmentAndDeletedFalseOrderByHierarchyLevelAscDisplayOrderAsc(
            Department department
    );

    List<Designation> findAllByHierarchyLevelAndDeletedFalseOrderByDisplayOrderAsc(
            Integer hierarchyLevel
    );

    // =========================================================================
    // Dropdown
    // =========================================================================

    List<Designation> findAllByStatusAndDeletedFalseOrderByNameAsc(
            Status status
    );

    // =========================================================================
    // Count
    // =========================================================================

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(
            Status status
    );

    long countByDepartmentAndDeletedFalse(
            Department department
    );

    long countByHierarchyLevelAndDeletedFalse(
            Integer hierarchyLevel
    );

}