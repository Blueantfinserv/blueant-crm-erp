package com.blueant_crm_erp.user.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.dto.request.ChangeTeamStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateTeamRequest;
import com.blueant_crm_erp.user.dto.request.TeamSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateTeamRequest;
import com.blueant_crm_erp.user.dto.response.TeamDropdownResponse;
import com.blueant_crm_erp.user.dto.response.TeamResponse;
import com.blueant_crm_erp.user.dto.response.TeamSummaryResponse;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.mapper.TeamMapper;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.service.TeamService;
import com.blueant_crm_erp.user.specification.TeamSpecification;
import com.blueant_crm_erp.user.validator.TeamValidator;
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
 * Team Service Implementation
 * =============================================================================
 *
 * Enterprise implementation of {@link TeamService}.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create / Update / Soft-Delete / Restore Team
 * • Change Team Status
 * • Query Teams (by id, name, paged search, dropdown, listing)
 * • Existence Checks and Count Queries
 *
 * Design Principles
 * -----------------------------------------------------------------------------
 * • All uniqueness and delete-safety validation delegated to {@link TeamValidator}.
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
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamValidator  teamValidator;
    private final TeamMapper     teamMapper;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        log.info("Creating team with code: {}", request.getCode());
        teamValidator.validateTeamCode(request.getCode());
        teamValidator.validateTeamName(request.getName());

        Team team = teamMapper.toEntity(request);
        team.setStatus(Status.ACTIVE);

        team = teamRepository.save(team);
        log.info("Successfully created team with id: {}", team.getId());
        return teamMapper.toResponse(team);
    }

    // =========================================================================
    // Update
    // =========================================================================

    @Override
    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        log.info("Updating team with id: {}", teamId);
        teamValidator.validateUpdate(teamId, request.getCode(), request.getName());

        Team team = teamValidator.validateTeam(teamId);
        teamMapper.updateEntity(request, team);

        team = teamRepository.save(team);
        log.info("Successfully updated team with id: {}", teamId);
        return teamMapper.toResponse(team);
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @Override
    @Transactional
    public void deleteTeam(Long teamId) {
        log.info("Deleting team with id: {}", teamId);
        // validateDelete internally checks for assigned users before allowing deletion
        teamValidator.validateDelete(teamId);
        Team team = teamValidator.validateTeam(teamId);
        team.markAsDeleted(SecurityUtil.getCurrentUsername());
        teamRepository.save(team);
        log.info("Successfully deleted team with id: {}", teamId);
    }

    @Override
    @Transactional
    public void restoreTeam(Long teamId) {
        log.info("Restoring team with id: {}", teamId);
        Team team = teamRepository.findByIdAndDeletedTrue(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        team.restore();
        teamRepository.save(team);
        log.info("Successfully restored team with id: {}", teamId);
    }

    // =========================================================================
    // Status
    // =========================================================================

    @Override
    @Transactional
    public TeamResponse changeTeamStatus(Long teamId, ChangeTeamStatusRequest request) {
        log.info("Changing status of team with id: {} to {}", teamId, request.getStatus());
        Team team = teamValidator.validateTeam(teamId);
        team.setStatus(request.getStatus());
        team = teamRepository.save(team);
        log.info("Successfully changed status of team with id: {}", teamId);
        return teamMapper.toResponse(team);
    }

    // =========================================================================
    // Query — Single
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long teamId) {
        Team team = teamValidator.validateTeam(teamId);
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamByName(String teamName) {
        Team team = teamRepository.findByTeamNameIgnoreCaseAndDeletedFalse(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        return teamMapper.toResponse(team);
    }

    // =========================================================================
    // Query — Collection
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<TeamSummaryResponse> getAllTeams() {
        return teamMapper.toSummaryList(
                teamRepository.findAllByDeletedFalseOrderByDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamSummaryResponse> getAllTeamsIncludingDeleted() {
        return teamMapper.toSummaryList(
                teamRepository.findAllByOrderByDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamSummaryResponse> searchTeams(TeamSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Team> page = teamRepository.findAll(TeamSpecification.search(request), pageable);
        return PageResponse.of(page.map(teamMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDropdownResponse> getTeamDropdown() {
        return teamMapper.toDropdownList(
                teamRepository.findAllByStatusAndDeletedFalseOrderByTeamNameAsc(Status.ACTIVE));
    }

    // =========================================================================
    // Existence Checks
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long teamId) {
        return teamRepository.existsByIdAndDeletedFalse(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String teamName) {
        return teamRepository.existsByTeamNameIgnoreCaseAndDeletedFalse(teamName);
    }

    // =========================================================================
    // Count
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public long countTeams() {
        return teamRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveTeams() {
        return teamRepository.countByDeletedFalse();
    }
}
