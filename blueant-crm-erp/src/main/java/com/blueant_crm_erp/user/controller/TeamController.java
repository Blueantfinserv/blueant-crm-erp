package com.blueant_crm_erp.user.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.constant.TeamConstants;
import com.blueant_crm_erp.user.dto.request.ChangeTeamStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateTeamRequest;
import com.blueant_crm_erp.user.dto.request.TeamSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateTeamRequest;
import com.blueant_crm_erp.user.dto.response.TeamDropdownResponse;
import com.blueant_crm_erp.user.dto.response.TeamResponse;
import com.blueant_crm_erp.user.dto.response.TeamSummaryResponse;
import com.blueant_crm_erp.user.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(TeamConstants.API_BASE)
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "APIs for managing teams")
public class TeamController {

    private final TeamService teamService;

    // =========================================================================
    // Create
    // =========================================================================

    @PostMapping
    @Operation(
            summary     = "Create New Team",
            description = "Creates a new team and validates code and name uniqueness."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name")
    })
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody CreateTeamRequest request) {
        log.info("REST request to create team with code: {}", request.getCode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.createTeam(request)));
    }

    // =========================================================================
    // Update
    // =========================================================================

    @PutMapping(TeamConstants.TEAM_ID)
    @Operation(
            summary     = "Update Existing Team",
            description = "Updates an existing team with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "ID of the team to update") @PathVariable Long teamId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateTeamRequest request) {
        log.info("REST request to update team with id: {}", teamId);
        return ResponseEntity.ok(ApiResponse.success(teamService.updateTeam(teamId, request)));
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @DeleteMapping(TeamConstants.TEAM_ID)
    @Operation(
            summary     = "Delete Team",
            description = "Marks a specific team as deleted if no active entities are assigned."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Team deleted — no content returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Team has assigned users"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteTeam(
            @Parameter(description = "ID of the team to delete") @PathVariable Long teamId) {
        log.info("REST request to delete team with id: {}", teamId);
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(TeamConstants.RESTORE)
    @Operation(
            summary     = "Restore Deleted Team",
            description = "Restores a previously deleted team."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deleted team not found")
    })
    public ResponseEntity<ApiResponse<String>> restoreTeam(
            @Parameter(description = "ID of the team to restore") @PathVariable Long teamId) {
        log.info("REST request to restore team with id: {}", teamId);
        teamService.restoreTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success("Team restored successfully."));
    }

    // =========================================================================
    // Status
    // =========================================================================

    @PatchMapping(TeamConstants.STATUS)
    @Operation(
            summary     = "Change Team Status",
            description = "Changes the active status of a specific team."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponse>> changeTeamStatus(
            @Parameter(description = "ID of the team") @PathVariable Long teamId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangeTeamStatusRequest request) {
        log.info("REST request to change status of team with id: {}", teamId);
        return ResponseEntity.ok(ApiResponse.success(teamService.changeTeamStatus(teamId, request)));
    }

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping(TeamConstants.TEAM_ID)
    @Operation(
            summary     = "Get Team By ID",
            description = "Retrieves team details by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(
            @Parameter(description = "ID of the team to retrieve") @PathVariable Long teamId) {
        log.info("REST request to get team with id: {}", teamId);
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamById(teamId)));
    }

    @GetMapping(TeamConstants.NAME)
    @Operation(
            summary     = "Get Team By Name",
            description = "Retrieves team details by its name."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamByName(
            @Parameter(description = "Name of the team to retrieve") @PathVariable String teamName) {
        log.info("REST request to get team with name: {}", teamName);
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamByName(teamName)));
    }

    @GetMapping
    @Operation(
            summary     = "Get All Active Teams",
            description = "Retrieves a list of all active teams."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team list returned")
    })
    public ResponseEntity<ApiResponse<List<TeamSummaryResponse>>> getAllTeams() {
        log.info("REST request to get all teams");
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllTeams()));
    }

    @GetMapping(TeamConstants.ALL)
    @Operation(
            summary     = "Get All Teams Including Deleted",
            description = "Retrieves a list of all teams including deleted ones."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team list returned")
    })
    public ResponseEntity<ApiResponse<List<TeamSummaryResponse>>> getAllTeamsIncludingDeleted() {
        log.info("REST request to get all teams including deleted");
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllTeamsIncludingDeleted()));
    }

    @PostMapping(TeamConstants.SEARCH)
    @Operation(
            summary     = "Search Teams",
            description = "Searches for teams based on specific criteria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<PageResponse<TeamSummaryResponse>>> searchTeams(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody TeamSearchRequest request) {
        log.info("REST request to search teams");
        return ResponseEntity.ok(ApiResponse.success(teamService.searchTeams(request)));
    }

    @GetMapping(TeamConstants.DROPDOWN)
    @Operation(
            summary     = "Get Team Dropdown",
            description = "Retrieves a lightweight list of teams formatted for dropdowns."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dropdown list returned")
    })
    public ResponseEntity<ApiResponse<List<TeamDropdownResponse>>> getTeamDropdown() {
        log.info("REST request to get team dropdown");
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamDropdown()));
    }
}
