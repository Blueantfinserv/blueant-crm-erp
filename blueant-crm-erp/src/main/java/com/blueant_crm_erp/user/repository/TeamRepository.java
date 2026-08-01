package com.blueant_crm_erp.user.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Team Repository
 * =============================================================================
 *
 * Repository for Team Entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • CRUD Operations
 * • Dynamic Search (Specification)
 * • Soft Delete Support
 * • Duplicate Validation
 * • Department Wise Teams
 * • Dropdown APIs
 * • Dashboard Statistics
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 * =============================================================================
 */
@Repository
public interface TeamRepository extends
        JpaRepository<Team, Long>,
        JpaSpecificationExecutor<Team> {

    // =========================================================================
    // Find By Id
    // =========================================================================

    Optional<Team> findByIdAndDeletedFalse(Long id);

    Optional<Team> findByIdAndDeletedTrue(Long id);

    // =========================================================================
    // Find By Team Code
    // =========================================================================

    Optional<Team> findByTeamCodeIgnoreCase(String teamCode);

    Optional<Team> findByTeamCodeIgnoreCaseAndDeletedFalse(String teamCode);

    // =========================================================================
    // Find By Team Name
    // =========================================================================

    Optional<Team> findByTeamNameIgnoreCase(String teamName);

    Optional<Team> findByTeamNameIgnoreCaseAndDeletedFalse(String teamName);

    // =========================================================================
    // Exists Validation
    // =========================================================================

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByTeamCodeIgnoreCase(String teamCode);

    boolean existsByTeamCodeIgnoreCaseAndDeletedFalse(String teamCode);

    boolean existsByTeamNameIgnoreCase(String teamName);

    boolean existsByTeamNameIgnoreCaseAndDeletedFalse(String teamName);

    // =========================================================================
    // Listing
    // =========================================================================

    List<Team> findAllByDeletedFalseOrderByDisplayOrderAsc();

    List<Team> findAllByOrderByDisplayOrderAsc();

    List<Team> findAllByStatusAndDeletedFalseOrderByDisplayOrderAsc(
            Status status
    );

    List<Team> findAllByDepartmentAndDeletedFalseOrderByDisplayOrderAsc(
            Department department
    );

    // =========================================================================
    // Dropdown
    // =========================================================================

    List<Team> findAllByStatusAndDeletedFalseOrderByTeamNameAsc(
            Status status
    );

    // =========================================================================
    // Count
    // =========================================================================

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(Status status);

    long countByDepartmentAndDeletedFalse(
            Department department
    );

}