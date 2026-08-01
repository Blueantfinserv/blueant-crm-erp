package com.blueant_crm_erp.user.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.dto.request.ChangeDepartmentStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDepartmentRequest;
import com.blueant_crm_erp.user.dto.request.DepartmentSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDepartmentRequest;
import com.blueant_crm_erp.user.dto.response.DepartmentDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentSummaryResponse;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.mapper.DepartmentMapper;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.service.DepartmentService;
import com.blueant_crm_erp.user.specification.DepartmentSpecification;
import com.blueant_crm_erp.user.validator.DepartmentValidator;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =============================================================================
 * Department Service Implementation
 * =============================================================================
 *
 * Enterprise implementation of {@link DepartmentService}.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create / Update / Soft-Delete / Restore Department
 * • Change Department Status
 * • Query Departments (by id, name, paged search, dropdown, listing)
 * • Existence Checks and Count Queries
 *
 * Design Principles
 * -----------------------------------------------------------------------------
 * • All uniqueness and delete-safety validation delegated to {@link DepartmentValidator}.
 * • Transactional boundaries are declared per method:
 *     - readOnly = true on all read-only operations
 *     - default (readOnly = false) on all write operations
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * @author BlueAnt CRM ERP Team
 * @since  1.0.0
 * =============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentValidator  departmentValidator;
    private final DepartmentMapper     departmentMapper;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating department with code: {}", request.getCode());
        departmentValidator.validateCode(request.getCode());
        departmentValidator.validateName(request.getName());

        Department department = departmentMapper.toEntity(request);
        department.setStatus(Status.ACTIVE);
        department.setDeleted(false);

        department = departmentRepository.save(department);
        log.info("Successfully created department with id: {}", department.getId());
        return departmentMapper.toResponse(department);
    }

    // =========================================================================
    // Update
    // =========================================================================

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long departmentId, UpdateDepartmentRequest request) {
        log.info("Updating department with id: {}", departmentId);
        departmentValidator.validateUpdate(departmentId, request.getCode(), request.getName());

        Department department = departmentValidator.validateDepartment(departmentId);
        departmentMapper.updateEntity(request, department);

        department = departmentRepository.save(department);
        log.info("Successfully updated department with id: {}", departmentId);
        return departmentMapper.toResponse(department);
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId) {
        log.info("Deleting department with id: {}", departmentId);
        // validateDelete internally checks for assigned users / designations / teams
        departmentValidator.validateDelete(departmentId);
        Department department = departmentValidator.validateDepartment(departmentId);
        department.markAsDeleted(SecurityUtil.getCurrentUsername());
        departmentRepository.save(department);
        log.info("Successfully deleted department with id: {}", departmentId);
    }

    @Override
    @Transactional
    public void restoreDepartment(Long departmentId) {
        log.info("Restoring department with id: {}", departmentId);
        Department department = departmentRepository.findByIdAndDeletedTrue(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        department.restore();
        departmentRepository.save(department);
        log.info("Successfully restored department with id: {}", departmentId);
    }

    // =========================================================================
    // Status
    // =========================================================================

    @Override
    @Transactional
    public DepartmentResponse changeDepartmentStatus(Long departmentId, ChangeDepartmentStatusRequest request) {
        log.info("Changing status of department with id: {} to {}", departmentId, request.getStatus());
        Department department = departmentValidator.validateDepartment(departmentId);
        department.setStatus(request.getStatus());
        department = departmentRepository.save(department);
        log.info("Successfully changed status of department with id: {}", departmentId);
        return departmentMapper.toResponse(department);
    }

    // =========================================================================
    // Query — Single
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long departmentId) {
        Department department = departmentValidator.validateDepartment(departmentId);
        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByName(String departmentName) {
        Department department = departmentRepository.findByNameIgnoreCaseAndDeletedFalse(departmentName)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        return departmentMapper.toResponse(department);
    }

    // =========================================================================
    // Query — Collection
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentSummaryResponse> getAllDepartments() {
        return departmentMapper.toSummaryList(
                departmentRepository.findAllByDeletedFalseOrderByDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentSummaryResponse> getAllDepartmentsIncludingDeleted() {
        return departmentMapper.toSummaryList(
                departmentRepository.findAllByOrderByDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentSummaryResponse> searchDepartments(DepartmentSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Department> page = departmentRepository.findAll(DepartmentSpecification.search(request), pageable);
        return PageResponse.of(page.map(departmentMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDropdownResponse> getDepartmentDropdown() {
        return departmentMapper.toDropdownList(
                departmentRepository.findAllByStatusAndDeletedFalseOrderByNameAsc(Status.ACTIVE));
    }

    // =========================================================================
    // Existence Checks
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long departmentId) {
        return departmentRepository.existsByIdAndDeletedFalse(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String departmentName) {
        return departmentRepository.existsByNameIgnoreCaseAndDeletedFalse(departmentName);
    }

    // =========================================================================
    // Count
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDepartments() {
        return departmentRepository.countByDeletedFalse();
    }
}
