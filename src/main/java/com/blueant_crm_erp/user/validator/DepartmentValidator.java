package com.blueant_crm_erp.user.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.DuplicateResourceException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * =============================================================================
 * Department Validator
 * =============================================================================
 *
 * Business Validator for Department Management.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Department Validation
 * • Duplicate Validation
 * • Business Rule Validation
 * • Delete Validation
 * • Soft Delete Validation
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class DepartmentValidator {

    private final DepartmentRepository departmentRepository;

    // =========================================================================
    // Department Validation
    // =========================================================================

    public Department validateDepartment(Long departmentId) {

        return departmentRepository
                .findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found."
                        ));
    }

    // =========================================================================
    // Department Code Validation
    // =========================================================================

    public void validateCode(String code) {

        if (!StringUtils.hasText(code)) {
            throw new BadRequestException(
                    "Department code is required."
            );
        }

        if (departmentRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw new DuplicateResourceException(
                    "Department code already exists."
            );
        }
    }

    // =========================================================================
    // Department Name Validation
    // =========================================================================

    public void validateName(String name) {

        if (!StringUtils.hasText(name)) {
            throw new BadRequestException(
                    "Department name is required."
            );
        }

        if (departmentRepository.existsByNameIgnoreCaseAndDeletedFalse(name)) {
            throw new DuplicateResourceException(
                    "Department name already exists."
            );
        }
    }

    // =========================================================================
    // Update Validation
    // =========================================================================

    public void validateUpdate(
            Long departmentId,
            String code,
            String name
    ) {

        Department department = validateDepartment(departmentId);

        if (!department.getCode().equalsIgnoreCase(code)
                && departmentRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {

            throw new DuplicateResourceException(
                    "Department code already exists."
            );
        }

        if (!department.getName().equalsIgnoreCase(name)
                && departmentRepository.existsByNameIgnoreCaseAndDeletedFalse(name)) {

            throw new DuplicateResourceException(
                    "Department name already exists."
            );
        }
    }

    // =========================================================================
    // Delete Validation
    // =========================================================================

    public void validateDelete(Long departmentId) {

        Department department = validateDepartment(departmentId);

        if (!department.getUsers().isEmpty()) {
            throw new BadRequestException(
                    "Department cannot be deleted because users are assigned to it."
            );
        }

        if (!department.getDesignations().isEmpty()) {
            throw new BadRequestException(
                    "Department cannot be deleted because designations are assigned to it."
            );
        }

        if (!department.getTeams().isEmpty()) {
            throw new BadRequestException(
                    "Department cannot be deleted because teams are assigned to it."
            );
        }
    }

}