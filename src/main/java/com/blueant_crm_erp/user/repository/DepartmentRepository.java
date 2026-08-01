package com.blueant_crm_erp.user.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Department Repository
 * =============================================================================
 *
 * Repository for Department Entity.
 *
 * Responsibilities:
 * -----------------------------------------------------------------------------
 * • CRUD Operations
 * • Dynamic Search (Specification)
 * • Soft Delete Support
 * • Duplicate Validation
 * • Department Listing
 * • Dropdown APIs
 * • Dashboard Counts
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Repository
public interface DepartmentRepository extends
        JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {

    // =========================================================================
    // Find By Id
    // =========================================================================

    /**
     * Find active department by id.
     *
     * @param id department id
     * @return department
     */
    Optional<Department> findByIdAndDeletedFalse(Long id);

    /**
     * Find deleted department by id.
     *
     * @param id department id
     * @return department
     */
    Optional<Department> findByIdAndDeletedTrue(Long id);

    // =========================================================================
    // Find By Name
    // =========================================================================

    /**
     * Find department by name.
     *
     * @param name department name
     * @return department
     */
    Optional<Department> findByNameIgnoreCase(String name);

    /**
     * Find active department by name.
     *
     * @param name department name
     * @return department
     */
    Optional<Department> findByNameIgnoreCaseAndDeletedFalse(String name);

    // =========================================================================
    // Find By Code
    // =========================================================================

    /**
     * Find department by code.
     *
     * @param code department code
     * @return department
     */
    Optional<Department> findByCodeIgnoreCase(String code);

    /**
     * Find active department by code.
     *
     * @param code department code
     * @return department
     */
    Optional<Department> findByCodeIgnoreCaseAndDeletedFalse(String code);

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

    /**
     * Returns all active departments.
     */
    List<Department> findAllByDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Returns all departments.
     */
    List<Department> findAllByOrderByDisplayOrderAsc();

    /**
     * Returns active departments by status.
     *
     * @param status department status
     * @return department list
     */
    List<Department> findAllByStatusAndDeletedFalseOrderByDisplayOrderAsc(
            Status status
    );

    // =========================================================================
    // Dropdown
    // =========================================================================

    /**
     * Returns departments for dropdown.
     *
     * @param status department status
     * @return department list
     */
    List<Department> findAllByStatusAndDeletedFalseOrderByNameAsc(
            Status status
    );

    // =========================================================================
    // Count
    // =========================================================================

    /**
     * Total active departments.
     *
     * @return total active departments
     */
    long countByDeletedFalse();

    /**
     * Count departments by status.
     *
     * @param status department status
     * @return total departments
     */
    long countByStatusAndDeletedFalse(
            Status status
    );

}