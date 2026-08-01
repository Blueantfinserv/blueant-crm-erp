package com.blueant_crm_erp.role.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * =============================================================================
 * Role Validator
 * =============================================================================
 *
 * Performs business validations for Role Management.
 *
 * Responsibilities:
 * - Validate Role Creation
 * - Validate Role Update
 * - Validate Role Status Change
 * - Validate Role Deletion
 * - Validate Duplicate Role Name
 * - Validate Duplicate Role Code
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class RoleValidator {

    /**
     * Repository for role validation.
     */
    private final RoleRepository roleRepository;

    /**
     * Validate role creation.
     *
     * @param roleName role name
     * @param roleCode role code
     */
    public void validateCreateRole(String roleName,
                                   String roleCode) {

        if (!StringUtils.hasText(roleName)) {
            throw new BadRequestException("Role name is required.");
        }
        if (!StringUtils.hasText(roleCode)) {
            throw new BadRequestException("Role code is required.");
        }

        if (roleRepository.existsByNameIgnoreCase(roleName)) {
            throw new BadRequestException("Role name already exists.");
        }

        if (roleRepository.existsByCodeIgnoreCase(roleCode)) {
            throw new BadRequestException("Role code already exists.");
        }
    }

    /**
     * Validate role update.
     *
     * @param roleId role id
     * @param roleName role name
     * @param roleCode role code
     */
    public void validateUpdateRole(Long roleId,
                                   String roleName,
                                   String roleCode) {

        Role existingRole = validateRoleExists(roleId);

        if (!existingRole.getName().equalsIgnoreCase(roleName) && roleRepository.existsByNameIgnoreCase(roleName)) {
            throw new BadRequestException("Role name already exists.");
        }

        if (!existingRole.getCode().equalsIgnoreCase(roleCode) && roleRepository.existsByCodeIgnoreCase(roleCode)) {
            throw new BadRequestException("Role code already exists.");
        }
    }

    /**
     * Validate role status change.
     *
     * @param roleId role id
     */
    public void validateRoleStatus(Long roleId) {
        Role role = validateRoleExists(roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("Cannot change status of a system role.");
        }
    }

    /**
     * Validate role deletion.
     *
     * @param roleId role id
     */
    public void validateDeleteRole(Long roleId) {
        Role role = validateRoleExists(roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("Cannot delete a system role.");
        }
        // TODO: Check users assigned (if user repository is available)
    }

    /**
     * Validates and returns the role if it exists.
     *
     * @param roleId role id
     * @return Role
     */
    public Role validateRoleExists(Long roleId) {
        return roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }
}
