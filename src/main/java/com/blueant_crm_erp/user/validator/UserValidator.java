package com.blueant_crm_erp.user.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;

import com.blueant_crm_erp.exception.common.DuplicateResourceException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * User Validator
 * =============================================================================
 *
 * Business Validator for User Module.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Duplicate Validation
 * • Foreign Key Validation
 * • Reporting Manager Validation
 * • Business Rule Validation
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final TeamRepository teamRepository;

    // =========================================================================
    // User Validation
    // =========================================================================

    public User validateUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    // =========================================================================
// Duplicate Validation
// =========================================================================

    public void validateEmployeeCode(String employeeCode) {

        if (userRepository.existsByEmployeeCodeIgnoreCaseAndDeletedFalse(employeeCode)) {
            throw new DuplicateResourceException(
                    "Employee code already exists."
            );
        }
    }

    public void validateEmail(String email) {

        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)) {
            throw new DuplicateResourceException(
                    "Official email already exists."
            );
        }
    }

    public void validateMobile(String mobileNumber) {

        if (userRepository.existsByMobileNumberAndDeletedFalse(mobileNumber)) {
            throw new DuplicateResourceException(
                    "Official mobile already exists."
            );
        }
    }

    // =========================================================================
    // Role Validation
    // =========================================================================

    public void validateRole(Long roleId) {

        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found.");
        }
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
    // Team Validation
    // =========================================================================

    public Team validateTeam(Long teamId) {

        return teamRepository
                .findByIdAndDeletedFalse(teamId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found."
                        ));
    }

    // =========================================================================
    // Reporting Manager Validation
    // =========================================================================

    public User validateReportingManager(Long reportingManagerId) {

        if (reportingManagerId == null) {
            return null;
        }

        return userRepository
                .findByIdAndDeletedFalse(reportingManagerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reporting manager not found."
                        ));
    }

    public void validateReportingManager(
            Long userId,
            Long reportingManagerId
    ) {

        if (reportingManagerId == null) {
            return;
        }

        if (userId.equals(reportingManagerId)) {
            throw new BadRequestException(
                    "User cannot report to himself."
            );
        }
    }

}