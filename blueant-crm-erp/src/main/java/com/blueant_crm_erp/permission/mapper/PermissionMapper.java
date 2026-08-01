package com.blueant_crm_erp.permission.mapper;

import com.blueant_crm_erp.permission.dto.request.CreatePermissionRequest;
import com.blueant_crm_erp.permission.dto.request.UpdatePermissionRequest;
import com.blueant_crm_erp.permission.dto.response.PermissionDropdownResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionSummaryResponse;
import com.blueant_crm_erp.permission.entity.Permission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * =============================================================================
 * Permission Mapper
 * =============================================================================
 *
 * Converts Permission Entity <-> DTO objects.
 *
 * Responsibilities
 * ----------------
 * • Create Request -> Entity
 * • Update Request -> Existing Entity
 * • Entity -> Response
 * • Entity -> Summary Response
 * • Entity -> Dropdown Response
 * • List Conversion
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

    /**
     * =========================================================================
     * Create Request -> Entity
     * =========================================================================
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    /**
     * =========================================================================
     * Entity -> Complete Response
     * =========================================================================
     */
    PermissionResponse toResponse(Permission permission);

    /**
     * =========================================================================
     * Entity -> Summary Response
     * =========================================================================
     */
    PermissionSummaryResponse toSummaryResponse(Permission permission);

    /**
     * =========================================================================
     * Entity -> Dropdown Response
     * =========================================================================
     */
    PermissionDropdownResponse toDropdownResponse(Permission permission);

    /**
     * =========================================================================
     * Entity List -> Response List
     * =========================================================================
     */
    List<PermissionResponse> toResponseList(
            List<Permission> permissions
    );

    /**
     * =========================================================================
     * Entity List -> Summary List
     * =========================================================================
     */
    List<PermissionSummaryResponse> toSummaryResponseList(
            List<Permission> permissions
    );

    /**
     * =========================================================================
     * Entity List -> Dropdown List
     * =========================================================================
     */
    List<PermissionDropdownResponse> toDropdownResponseList(
            List<Permission> permissions
    );

    /**
     * =========================================================================
     * Update Existing Entity
     * =========================================================================
     *
     * Only updates non-null fields.
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromRequest(
            UpdatePermissionRequest request,
            @MappingTarget Permission permission
    );

}