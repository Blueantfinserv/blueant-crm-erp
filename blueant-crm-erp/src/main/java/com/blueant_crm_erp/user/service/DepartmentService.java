package com.blueant_crm_erp.user.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.dto.request.ChangeDepartmentStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDepartmentRequest;
import com.blueant_crm_erp.user.dto.request.DepartmentSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDepartmentRequest;
import com.blueant_crm_erp.user.dto.response.DepartmentDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentSummaryResponse;

import java.util.List;

/**
 * =============================================================================
 * Department Service
 * =============================================================================
 *
 * Business operations for Department Management.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Departments
 * -----------------------------------------------------------------------------
 * • Sales
 * • Operations
 * • HR
 * • Accounts
 * • Helpdesk
 * • Mutual Fund
 * • Insurance
 * • Share
 * • Loan
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create Department
 * • Update Department
 * • Delete Department
 * • Restore Department
 * • Change Department Status
 * • Get Department
 * • Search Departments
 * • Department Dropdown
 * • Count Departments
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
public interface DepartmentService {

    /**
     * Create Department.
     */
    DepartmentResponse createDepartment(
            CreateDepartmentRequest request
    );

    /**
     * Update Department.
     */
    DepartmentResponse updateDepartment(
            Long departmentId,
            UpdateDepartmentRequest request
    );

    /**
     * Soft Delete Department.
     */
    void deleteDepartment(
            Long departmentId
    );

    /**
     * Restore Deleted Department.
     */
    void restoreDepartment(
            Long departmentId
    );

    /**
     * Change Department Status.
     */
    DepartmentResponse changeDepartmentStatus(
            Long departmentId,
            ChangeDepartmentStatusRequest request
    );

    /**
     * Get Department By Id.
     */
    DepartmentResponse getDepartmentById(
            Long departmentId
    );

    /**
     * Get Department By Name.
     */
    DepartmentResponse getDepartmentByName(
            String departmentName
    );

    /**
     * Get All Active Departments.
     */
    List<DepartmentSummaryResponse> getAllDepartments();

    /**
     * Get All Departments Including Deleted.
     */
    List<DepartmentSummaryResponse> getAllDepartmentsIncludingDeleted();

    /**
     * Search Departments.
     */
    PageResponse<DepartmentSummaryResponse> searchDepartments(
            DepartmentSearchRequest request
    );

    /**
     * Department Dropdown.
     */
    List<DepartmentDropdownResponse> getDepartmentDropdown();

    /**
     * Check Department Exists.
     */
    boolean existsById(
            Long departmentId
    );

    /**
     * Check Department Name Exists.
     */
    boolean existsByName(
            String departmentName
    );

    /**
     * Count Total Departments.
     */
    long countDepartments();

    /**
     * Count Active Departments.
     */
    long countActiveDepartments();

}