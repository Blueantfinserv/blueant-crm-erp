package com.blueant_crm_erp.role.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.role.dto.request.ChangeRoleStatusRequest;
import com.blueant_crm_erp.role.dto.request.CreateRoleRequest;
import com.blueant_crm_erp.role.dto.request.UpdateRoleRequest;
import com.blueant_crm_erp.role.dto.response.RoleResponse;
import com.blueant_crm_erp.role.dto.response.RoleSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(Long roleId, UpdateRoleRequest request);

    RoleResponse changeRoleStatus(Long roleId,
                                  ChangeRoleStatusRequest request);

    RoleResponse getRoleById(Long roleId);

    List<RoleSummaryResponse> getAllRoles();

    PageResponse<RoleSummaryResponse> getAllRoles(Pageable pageable);

    PageResponse<RoleSummaryResponse> searchRoles(String keyword,
                                                  Pageable pageable);

    void deleteRole(Long roleId);

    boolean exists(Long roleId);

}