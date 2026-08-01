package com.blueant_crm_erp.user.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * User Repository
 * =============================================================================
 *
 * Repository for User Entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • CRUD Operations
 * • Dynamic Search (Specification)
 * • Authentication Support
 * • Duplicate Validation
 * • Reporting Hierarchy
 * • Team Management
 * • Dropdown APIs
 * • Dashboard Statistics
 * • Soft Delete Support
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Repository
public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // =========================================================================
    // Find By Id
    // =========================================================================

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByIdAndDeletedTrue(Long id);

    // =========================================================================
    // Employee Code
    // =========================================================================

    Optional<User> findByEmployeeCodeIgnoreCase(String employeeCode);

    Optional<User> findByEmployeeCodeIgnoreCaseAndDeletedFalse(
            String employeeCode
    );

    List<User> findAllByEmployeeCodeInIgnoreCaseAndDeletedFalse(
            java.util.Collection<String> employeeCodes
    );

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndDeletedFalse(
            String employeeCode
    );

    // =========================================================================
    // Email
    // =========================================================================

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(
            String email
    );

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(
            String email
    );

    /**
     * =========================================================================
     * Authentication
     * =========================================================================
     */

    /**
     * Finds user by Employee Code, Email or Mobile Number.
     *
     * Used by Spring Security during authentication.
     */
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
            String employeeCode,
            String email,
            String mobileNumber
    );

    /**
     * Finds user profile with all associations eager loaded.
     * Used for returning complete user details without N+1 queries.
     */
    @EntityGraph(attributePaths = {"role", "department", "designation", "team", "reportingManager"})
    Optional<User> findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(
            String employeeCode
    );

    // =========================================================================
    // Mobile
    // =========================================================================

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByMobileNumberAndDeletedFalse(
            String mobileNumber
    );

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByMobileNumberAndDeletedFalse(
            String mobileNumber
    );

    // =========================================================================
    // Exists
    // =========================================================================

    boolean existsByIdAndDeletedFalse(Long id);

    // =========================================================================
    // Listing
    // =========================================================================

    List<User> findAllByDeletedFalseOrderByFirstNameAsc();

    List<User> findAllByOrderByFirstNameAsc();

    List<User> findAllByStatusAndDeletedFalseOrderByFirstNameAsc(
            Status status
    );

    List<User> findAllByDepartmentAndDeletedFalseOrderByFirstNameAsc(
            Department department
    );

    List<User> findAllByDesignationAndDeletedFalseOrderByFirstNameAsc(
            Designation designation
    );

    List<User> findAllByTeamAndDeletedFalseOrderByFirstNameAsc(
            Team team
    );

    List<User> findAllByRoleAndDeletedFalseOrderByFirstNameAsc(
            Role role
    );

    // =========================================================================
    // Reporting Manager
    // =========================================================================

    List<User> findAllByReportingManagerAndDeletedFalseOrderByFirstNameAsc(
            User reportingManager
    );

    List<User> findAllByReportingManagerIdAndDeletedFalseOrderByFirstNameAsc(
            Long reportingManagerId
    );

    // =========================================================================
    // Dropdown
    // =========================================================================

    List<User> findAllByStatusAndAccountEnabledTrueAndDeletedFalseOrderByFirstNameAsc(
            Status status
    );

    // =========================================================================
    // Dashboard Count
    // =========================================================================

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(
            Status status
    );

    long countByDepartmentAndDeletedFalse(
            Department department
    );

    long countByDesignationAndDeletedFalse(
            Designation designation
    );

    long countByTeamAndDeletedFalse(
            Team team
    );

    long countByRoleAndDeletedFalse(
            Role role
    );

    long countByReportingManagerAndDeletedFalse(
            User reportingManager
    );

}