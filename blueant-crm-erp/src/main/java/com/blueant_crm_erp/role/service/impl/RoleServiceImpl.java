package com.blueant_crm_erp.role.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.role.dto.request.ChangeRoleStatusRequest;
import com.blueant_crm_erp.role.dto.request.CreateRoleRequest;
import com.blueant_crm_erp.role.dto.request.UpdateRoleRequest;
import com.blueant_crm_erp.role.dto.response.RoleResponse;
import com.blueant_crm_erp.role.dto.response.RoleSummaryResponse;
import com.blueant_crm_erp.role.mapper.RoleMapper;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.role.service.RoleService;
import com.blueant_crm_erp.role.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RoleValidator roleValidator;

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    public RoleResponse changeRoleStatus(Long roleId, ChangeRoleStatusRequest request) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleSummaryResponse> getAllRoles() {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleSummaryResponse> getAllRoles(Pageable pageable) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleSummaryResponse> searchRoles(String keyword, Pageable pageable) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    public void deleteRole(Long roleId) {
        throw new UnsupportedOperationException("Implementation pending.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long roleId) {
        return roleRepository.existsById(roleId);
    }
}