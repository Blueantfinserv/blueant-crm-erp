package com.blueant_crm_erp.role.mapper;

import com.blueant_crm_erp.role.dto.request.CreateRoleRequest;
import com.blueant_crm_erp.role.dto.request.UpdateRoleRequest;
import com.blueant_crm_erp.role.dto.response.RoleResponse;
import com.blueant_crm_erp.role.dto.response.RoleSummaryResponse;
import com.blueant_crm_erp.role.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * =============================================================================
 * Role Mapper
 * =============================================================================
 *
 * Maps between Role Entity and DTOs.
 *
 * Responsibilities:
 * - Create Request -> Entity
 * - Update Request -> Existing Entity
 * - Entity -> Response
 * - Entity -> Summary Response
 * - Entity List -> Response List
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface RoleMapper {

    /**
     * Convert CreateRoleRequest to Role Entity.
     *
     * @param request create role request
     * @return role entity
     */
    Role toEntity(CreateRoleRequest request);

    /**
     * Update existing Role entity from UpdateRoleRequest.
     * Null values will be ignored.
     *
     * @param request update request
     * @param role existing role
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateRoleRequest request,
                      @MappingTarget Role role);

    /**
     * Convert Role Entity to RoleResponse.
     *
     * @param role role entity
     * @return role response
     */
    RoleResponse toResponse(Role role);

    /**
     * Convert Role Entity to RoleSummaryResponse.
     *
     * @param role role entity
     * @return summary response
     */
    RoleSummaryResponse toSummaryResponse(Role role);

    /**
     * Convert Role List to Response List.
     *
     * @param roles role entities
     * @return response list
     */
    List<RoleResponse> toResponseList(List<Role> roles);

    /**
     * Convert Role List to Summary Response List.
     *
     * @param roles role entities
     * @return summary response list
     */
    List<RoleSummaryResponse> toSummaryResponseList(List<Role> roles);

}