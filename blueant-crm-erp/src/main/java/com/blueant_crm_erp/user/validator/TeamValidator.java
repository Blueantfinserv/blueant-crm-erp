package com.blueant_crm_erp.user.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.DuplicateResourceException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * =============================================================================
 * Team Validator
 * =============================================================================
 *
 * Business Validator for Team Management.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Team Validation
 * • Duplicate Validation
 * • Department Validation
 * • Business Rule Validation
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
public class TeamValidator {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;

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

    public void validateTeamCode(String teamCode) {

        if (!StringUtils.hasText(teamCode)) {
            throw new BadRequestException(
                    "Team code is required."
            );
        }

        if (teamRepository.existsByTeamCodeIgnoreCaseAndDeletedFalse(teamCode)) {
            throw new DuplicateResourceException(
                    "Team code already exists."
            );
        }
    }

    public void validateTeamName(String teamName) {

        if (!StringUtils.hasText(teamName)) {
            throw new BadRequestException(
                    "Team name is required."
            );
        }

        if (teamRepository.existsByTeamNameIgnoreCaseAndDeletedFalse(teamName)) {
            throw new DuplicateResourceException(
                    "Team name already exists."
            );
        }
    }

    // =========================================================================
    // Update Validation
    // =========================================================================

    public void validateUpdate(
            Long teamId,
            String teamCode,
            String teamName
    ) {

        Team team = validateTeam(teamId);

        if (!team.getTeamCode().equalsIgnoreCase(teamCode)
                && teamRepository.existsByTeamCodeIgnoreCaseAndDeletedFalse(teamCode)) {

            throw new DuplicateResourceException(
                    "Team code already exists."
            );
        }

        if (!team.getTeamName().equalsIgnoreCase(teamName)
                && teamRepository.existsByTeamNameIgnoreCaseAndDeletedFalse(teamName)) {

            throw new DuplicateResourceException(
                    "Team name already exists."
            );
        }
    }

    // =========================================================================
    // Delete Validation
    // =========================================================================

    public void validateDelete(Long teamId) {

        Team team = validateTeam(teamId);

        if (!team.getUsers().isEmpty()) {
            throw new BadRequestException(
                    "Team cannot be deleted because users are assigned to it."
            );
        }
    }

}