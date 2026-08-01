package com.blueant_crm_erp.user.mapper;

import com.blueant_crm_erp.user.dto.request.CreateDepartmentRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDepartmentRequest;
import com.blueant_crm_erp.user.dto.response.DepartmentDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentSummaryResponse;
import com.blueant_crm_erp.user.entity.Department;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * =============================================================================
 * Department Mapper
 * =============================================================================
 *
 * MapStruct Mapper for Department Entity.
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
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface DepartmentMapper {

    // =========================================================================
    // Create Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "designations", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "users", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    Department toEntity(CreateDepartmentRequest request);

    // =========================================================================
    // Update Mapping
    // =========================================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "designations", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "users", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)

    void updateEntity(
            UpdateDepartmentRequest request,
            @MappingTarget Department department
    );

    // =========================================================================
    // Entity -> Response
    // =========================================================================

    @Mapping(
            target = "totalUsers",
            expression = "java(department.getUsers() == null ? 0 : department.getUsers().size())"
    )
    @Mapping(
            target = "totalTeams",
            expression = "java(department.getTeams() == null ? 0 : department.getTeams().size())"
    )
    DepartmentResponse toResponse(Department department);

    // =========================================================================
    // Entity -> Summary
    // =========================================================================

    @Mapping(
            target = "totalUsers",
            expression = "java(department.getUsers() == null ? 0 : department.getUsers().size())"
    )
    DepartmentSummaryResponse toSummary(Department department);

    List<DepartmentSummaryResponse> toSummaryList(
            List<Department> departments
    );

    // =========================================================================
    // Entity -> Dropdown
    // =========================================================================

    DepartmentDropdownResponse toDropdown(Department department);

    List<DepartmentDropdownResponse> toDropdownList(
            List<Department> departments
    );

}