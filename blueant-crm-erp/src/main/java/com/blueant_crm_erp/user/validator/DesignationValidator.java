package com.blueant_crm_erp.user.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.DuplicateResourceException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * =============================================================================
 * Designation Validator
 * =============================================================================
 *
 * Business Validator for Designation Management.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Designation Validation
 * • Department Validation
 * • Duplicate Validation
 * • Hierarchy Validation
 * • Business Rule Validation
 * • Delete Validation
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
public class DesignationValidator {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    // =========================================================================
    // Designation Validation
    // =========================================================================

    public Designation validateDesignation(Long designationId) {

        return designationRepository
                .findByIdAndDeletedFalse(designationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Designation not found."
                        ));
    }

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
    // Duplicate Validation
    // =========================================================================

    public void validateCode(String code) {

        if (!StringUtils.hasText(code)) {
            throw new BadRequestException(
                    "Designation code is required."
            );
        }

        if (designationRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw new DuplicateResourceException(
                    "Designation code already exists."
            );
        }
    }

    public void validateName(String name) {

        if (!StringUtils.hasText(name)) {
            throw new BadRequestException(
                    "Designation name is required."
            );
        }

        if (designationRepository.existsByNameIgnoreCaseAndDeletedFalse(name)) {
            throw new DuplicateResourceException(
                    "Designation name already exists."
            );
        }
    }

    // =========================================================================
    // Hierarchy Validation
    // =========================================================================

    public void validateHierarchyLevel(Integer hierarchyLevel) {

        if (hierarchyLevel == null) {
            throw new BadRequestException(
                    "Hierarchy level is required."
            );
        }

        if (hierarchyLevel < 1 || hierarchyLevel > 20) {
            throw new BadRequestException(
                    "Hierarchy level must be between 1 and 20."
            );
        }
    }

    // =========================================================================
    // Update Validation
    // =========================================================================

    public void validateUpdate(
            Long designationId,
            String code,
            String name
    ) {

        Designation designation = validateDesignation(designationId);

        if (!designation.getCode().equalsIgnoreCase(code)
                && designationRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {

            throw new DuplicateResourceException(
                    "Designation code already exists."
            );
        }

        if (!designation.getName().equalsIgnoreCase(name)
                && designationRepository.existsByNameIgnoreCaseAndDeletedFalse(name)) {

            throw new DuplicateResourceException(
                    "Designation name already exists."
            );
        }
    }

    // =========================================================================
    // Delete Validation
    // =========================================================================

    public void validateDelete(Long designationId) {

        Designation designation = validateDesignation(designationId);

        if (!designation.getUsers().isEmpty()) {
            throw new BadRequestException(
                    "Designation cannot be deleted because users are assigned to it."
            );
        }
    }

}