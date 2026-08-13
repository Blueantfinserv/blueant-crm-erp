package com.blueant_crm_erp.user.mapper;

import com.blueant_crm_erp.user.dto.request.CreateUserRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserRequest;
import com.blueant_crm_erp.user.dto.response.UserDropdownResponse;
import com.blueant_crm_erp.user.dto.response.UserResponse;
import com.blueant_crm_erp.user.dto.response.UserSummaryResponse;
import com.blueant_crm_erp.user.dto.response.UserProfileResponse;
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
 * User Mapper
 * =============================================================================
 *
 * MapStruct mapper for User Entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Entity → Response DTO
 * • Entity → Summary DTO
 * • Entity → Dropdown DTO
 * • Create Request → Entity
 * • Update Request → Existing Entity
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
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface UserMapper {

    // =========================================================================
    // Create Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "reportingUsers", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    User toEntity(CreateUserRequest request);

    // =========================================================================
    // Update Mapping
    // =========================================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "password", ignore = true)

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "reportingUsers", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    void updateEntity(
            UpdateUserRequest request,
            @MappingTarget User user
    );

    // =========================================================================
    // Entity → Response
    // =========================================================================

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "designationId", source = "designation.id")
    @Mapping(target = "designationName", source = "designation.name")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.teamName")
    @Mapping(target = "reportingManagerId", source = "reportingManager.id")
    @Mapping(
            target = "reportingManagerName",
            expression =
                    "java(user.getReportingManager() != null ? "
                            + "user.getReportingManager().getFullName() : null)"
    )
    UserResponse toResponse(User user);

    // =========================================================================
    // Entity → Summary
    // =========================================================================

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "designationName", source = "designation.name")
    @Mapping(target = "teamName", source = "team.teamName")
    UserSummaryResponse toSummary(User user);

    List<UserSummaryResponse> toSummaryList(List<User> users);

    // =========================================================================
    // Entity → Dropdown
    // =========================================================================

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDropdownResponse toDropdown(User user);

    List<UserDropdownResponse> toDropdownList(List<User> users);

    // =========================================================================
    // Profile Mapping
    // =========================================================================

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "designationName", source = "designation.name")
    @Mapping(target = "teamName", source = "team.teamName")
    @Mapping(target = "reportingManagerName", expression = "java(user.getReportingManager() != null ? user.getReportingManager().getFullName() : null)")
    @Mapping(target = "enabled", source = "accountEnabled")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserProfileResponse toProfileResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfile(com.blueant_crm_erp.user.dto.request.UpdateUserProfileRequest request, @MappingTarget User user);

}