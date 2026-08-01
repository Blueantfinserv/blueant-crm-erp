package com.blueant_crm_erp.user.mapper;

import com.blueant_crm_erp.user.dto.request.CreateDesignationRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDesignationRequest;
import com.blueant_crm_erp.user.dto.response.DesignationDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DesignationResponse;
import com.blueant_crm_erp.user.dto.response.DesignationSummaryResponse;
import com.blueant_crm_erp.user.entity.Designation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * =============================================================================
 * Designation Mapper
 * =============================================================================
 *
 * MapStruct Mapper for Designation Entity.
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
public interface DesignationMapper {

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

    Designation toEntity(CreateDesignationRequest request);

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
            UpdateDesignationRequest request,
            @MappingTarget Designation designation
    );

    // =========================================================================
    // Entity -> Response
    // =========================================================================

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalUsers",
            expression = "java(designation.getUsers() == null ? 0 : designation.getUsers().size())")
    DesignationResponse toResponse(Designation designation);

    // =========================================================================
    // Entity -> Summary
    // =========================================================================

    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalUsers",
            expression = "java(designation.getUsers() == null ? 0 : designation.getUsers().size())")
    DesignationSummaryResponse toSummary(Designation designation);

    List<DesignationSummaryResponse> toSummaryList(List<Designation> designations);

    // =========================================================================
    // Entity -> Dropdown
    // =========================================================================

    DesignationDropdownResponse toDropdown(Designation designation);

    List<DesignationDropdownResponse> toDropdownList(List<Designation> designations);

}