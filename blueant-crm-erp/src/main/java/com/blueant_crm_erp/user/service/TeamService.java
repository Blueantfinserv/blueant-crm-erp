package com.blueant_crm_erp.user.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.dto.request.ChangeTeamStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateTeamRequest;
import com.blueant_crm_erp.user.dto.request.TeamSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateTeamRequest;
import com.blueant_crm_erp.user.dto.response.TeamDropdownResponse;
import com.blueant_crm_erp.user.dto.response.TeamResponse;
import com.blueant_crm_erp.user.dto.response.TeamSummaryResponse;

import java.util.List;

/**
 * =============================================================================
 * Team Service
 * =============================================================================
 *
 * Business operations for Team Management.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Rohit
 *      ↓
 * Sales Manager
 *      ↓
 * Team Leader
 *      ↓
 * Sales Person
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create Team
 * • Update Team
 * • Delete Team
 * • Restore Team
 * • Change Team Status
 * • Search Teams
 * • Team Dropdown
 * • Team Statistics
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface TeamService {

    /**
     * Create Team.
     */
    TeamResponse createTeam(CreateTeamRequest request);

    /**
     * Update Team.
     */
    TeamResponse updateTeam(
            Long teamId,
            UpdateTeamRequest request
    );

    /**
     * Soft Delete Team.
     */
    void deleteTeam(Long teamId);

    /**
     * Restore Deleted Team.
     */
    void restoreTeam(Long teamId);

    /**
     * Change Team Status.
     */
    TeamResponse changeTeamStatus(
            Long teamId,
            ChangeTeamStatusRequest request
    );

    /**
     * Get Team By Id.
     */
    TeamResponse getTeamById(Long teamId);

    /**
     * Get Team By Name.
     */
    TeamResponse getTeamByName(String teamName);

    /**
     * Get All Active Teams.
     */
    List<TeamSummaryResponse> getAllTeams();

    /**
     * Get All Teams Including Deleted.
     */
    List<TeamSummaryResponse> getAllTeamsIncludingDeleted();

    /**
     * Search Teams.
     */
    PageResponse<TeamSummaryResponse> searchTeams(
            TeamSearchRequest request
    );

    /**
     * Team Dropdown.
     */
    List<TeamDropdownResponse> getTeamDropdown();

    /**
     * Check Team Exists.
     */
    boolean existsById(Long teamId);

    /**
     * Check Team Name Exists.
     */
    boolean existsByName(String teamName);

    /**
     * Count Total Teams.
     */
    long countTeams();

    /**
     * Count Active Teams.
     */
    long countActiveTeams();

}