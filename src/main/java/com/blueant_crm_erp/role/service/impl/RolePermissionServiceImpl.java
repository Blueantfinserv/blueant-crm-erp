package com.blueant_crm_erp.role.service.impl;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.DuplicateResourceException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.dto.request.AssignPermissionRequest;
import com.blueant_crm_erp.role.dto.response.RolePermissionResponse;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.role.service.RolePermissionService;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * Role Permission Service Implementation
 * =============================================================================
 *
 * Business implementation for Role Permission Management.
 *
 * Responsibilities:
 * - Assign Permissions to Role
 * - Remove Permissions from Role
 * - Get Role Permissions
 * - Validate Permission Assignment
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl implements RolePermissionService {

    /**
     * Role Permission Repository.
     */
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Role Repository.
     */
    private final RoleRepository roleRepository;

    /**
     * Permission Repository.
     */
    private final PermissionRepository permissionRepository;

    @Override
    public List<RolePermissionResponse> assignPermissions(
            AssignPermissionRequest request) {

        if (request == null) {
            throw new BadRequestException("Request cannot be null.");
        }

        Role role = roleRepository.findByIdAndDeletedFalse(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));

        if (role.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Role is not active.");
        }

        if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
            throw new BadRequestException("At least one permission must be selected.");
        }

        List<Long> uniquePermissionIds = request.getPermissionIds().stream().distinct().toList();
        List<Permission> permissions = new ArrayList<>();

        for (Long permissionId : uniquePermissionIds) {
            Permission permission = permissionRepository.findByIdAndDeletedFalse(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

            if (permission.getStatus() != Status.ACTIVE) {
                throw new BadRequestException("Permission is not active: " + permission.getName());
            }
            permissions.add(permission);
        }

        List<RolePermissionResponse> responses = new ArrayList<>();

        for (Permission p : permissions) {
            Optional<RolePermission> existingMappingOpt = rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), p.getId());
            RolePermission rolePermission;
            if (existingMappingOpt.isPresent()) {
                rolePermission = existingMappingOpt.get();
                if (!rolePermission.isDeleted()) {
                    throw new DuplicateResourceException(ExceptionMessage.DUPLICATE_MAPPING);
                }
                rolePermission.restore();
                rolePermission = rolePermissionRepository.save(rolePermission);
            } else {
                RolePermission newMapping = RolePermission.builder()
                        .role(role)
                        .permission(p)
                        .build();
                rolePermission = rolePermissionRepository.save(newMapping);
            }
            responses.add(mapToResponse(rolePermission));
        }

        return responses;
    }

    @Override
    public void removePermissions(
            AssignPermissionRequest request) {

        if (request == null) {
            throw new BadRequestException("Request cannot be null.");
        }

        Role role = roleRepository.findByIdAndDeletedFalse(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));

        if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
            throw new BadRequestException("At least one permission must be selected.");
        }

        for (Long permissionId : request.getPermissionIds()) {
            RolePermission mapping = rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), permissionId)
                    .filter(rp -> !rp.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.MAPPING_NOT_FOUND));

            mapping.markAsDeleted(SecurityUtil.getCurrentUsername());
            rolePermissionRepository.save(mapping);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getPermissionsByRoleId(
            Long roleId) {

        Role role = roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return rolePermissionRepository.findAllByRoleId(roleId).stream()
                .filter(rp -> !rp.isDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(
            Long roleId,
            Long permissionId) {

        return rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                .filter(rp -> !rp.isDeleted())
                .filter(rp -> !rp.getRole().isDeleted() && rp.getRole().getStatus() == Status.ACTIVE)
                .filter(rp -> !rp.getPermission().isDeleted() && rp.getPermission().getStatus() == Status.ACTIVE)
                .isPresent();
    }

    /**
     * Map RolePermission entity to RolePermissionResponse.
     */
    private RolePermissionResponse mapToResponse(RolePermission rp) {
        return RolePermissionResponse.builder()
                .id(rp.getId())
                .roleId(rp.getRole().getId())
                .roleName(rp.getRole().getName())
                .permissionId(rp.getPermission().getId())
                .permissionName(rp.getPermission().getName())
                .permissionCode(rp.getPermission().getCode())
                .moduleName(rp.getPermission().getModule())
                .description(rp.getPermission().getDescription())
                .status(rp.getPermission().getStatus())
                .assignedBy(rp.getCreatedBy())
                .assignedAt(rp.getCreatedAt())
                .build();
    }

}