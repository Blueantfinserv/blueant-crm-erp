package com.blueant_crm_erp.bootstrap.validator;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * Bootstrap Validator
 * =============================================================================
 *
 * Performs validation before executing bootstrap.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Prevent duplicate bootstrap
 * • Validate master data existence
 * • Validate default Super Admin
 * • Validate Roles
 * • Validate Permissions
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class BootstrapValidator {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final DepartmentRepository departmentRepository;

    private final DesignationRepository designationRepository;

    private final TeamRepository teamRepository;

    /**
     * Returns true if Super Admin exists.
     */
    public boolean hasSuperAdmin() {
        return userRepository.existsByEmployeeCodeIgnoreCase(BootstrapConstants.SUPER_ADMIN_EMPLOYEE_CODE);
    }

    /**
     * Returns true if required roles exist.
     */
    public boolean hasRoles() {
        return roleRepository.existsByCodeIgnoreCase(BootstrapConstants.ROLE_SUPER_ADMIN)
                && roleRepository.existsByCodeIgnoreCase(BootstrapConstants.ROLE_ADMIN)
                && roleRepository.existsByCodeIgnoreCase(BootstrapConstants.ROLE_BUSINESS_HEAD);
    }

    /**
     * Returns true if permissions already exist.
     */
    public boolean hasPermissions() {
        return permissionRepository.existsByCodeIgnoreCase(BootstrapConstants.PERM_USER_CREATE)
                && permissionRepository.existsByCodeIgnoreCase(BootstrapConstants.PERM_ROLE_CREATE);
    }

    /**
     * Returns true if required departments exist.
     */
    public boolean hasDepartments() {
        return departmentRepository.existsByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                && departmentRepository.existsByCodeIgnoreCase(BootstrapConstants.DEPT_HR)
                && departmentRepository.existsByCodeIgnoreCase(BootstrapConstants.DEPT_OPS);
    }

    /**
     * Returns true if required designations exist.
     */
    public boolean hasDesignations() {
        return designationRepository.existsByCodeIgnoreCase(BootstrapConstants.DESIG_BH)
                && designationRepository.existsByCodeIgnoreCase(BootstrapConstants.DESIG_SM)
                && designationRepository.existsByCodeIgnoreCase(BootstrapConstants.DESIG_HRM);
    }

    /**
     * Returns true if required teams exist.
     */
    public boolean hasTeams() {
        return teamRepository.existsByTeamCodeIgnoreCase(BootstrapConstants.TEAM_ST1)
                && teamRepository.existsByTeamCodeIgnoreCase(BootstrapConstants.TEAM_ST2);
    }

    /**
     * Returns true if database is already bootstrapped.
     */
    public boolean isBootstrapCompleted() {

        return hasSuperAdmin()
                && hasRoles()
                && hasPermissions()
                && hasDepartments()
                && hasDesignations()
                && hasTeams();

    }

}