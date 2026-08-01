package com.blueant_crm_erp.user.mapper;

import com.blueant_crm_erp.user.dto.request.CreateTeamRequest;
import com.blueant_crm_erp.user.dto.request.UpdateTeamRequest;
import com.blueant_crm_erp.user.dto.response.TeamDropdownResponse;
import com.blueant_crm_erp.user.dto.response.TeamResponse;
import com.blueant_crm_erp.user.dto.response.TeamSummaryResponse;
import com.blueant_crm_erp.user.dto.response.UserDropdownResponse;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * =============================================================================
 * Team Mapper
 * =============================================================================
 *
 * MapStruct mapper for Team Entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create Request -> Entity
 * • Update Request -> Existing Entity
 * • Entity -> Response DTO
 * • Entity -> Summary DTO
 * • Entity -> Dropdown DTO
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TeamMapper {

    // =========================================================================
    // Create Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "users", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    Team toEntity(CreateTeamRequest request);

    // =========================================================================
    // Update Mapping
    // =========================================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "users", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    void updateEntity(
            UpdateTeamRequest request,
            @MappingTarget Team team
    );

    // =========================================================================
    // Entity -> Response
    // =========================================================================

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalUsers", expression = "java(team.getTotalUsers())")
    TeamResponse toResponse(Team team);

    // =========================================================================
    // Entity -> Summary
    // =========================================================================

    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalMembers", expression = "java(team.getTotalUsers())")
    TeamSummaryResponse toSummary(Team team);

    List<TeamSummaryResponse> toSummaryList(List<Team> teams);

    // =========================================================================
    // Entity -> Dropdown
    // =========================================================================

    TeamDropdownResponse toDropdown(Team team);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDropdownResponse toDropdown(User user);

    List<TeamDropdownResponse> toDropdownList(List<Team> teams);

}