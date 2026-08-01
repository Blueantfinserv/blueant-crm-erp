package com.blueant_crm_erp.permission.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.dto.request.ChangePermissionStatusRequest;
import com.blueant_crm_erp.permission.dto.request.CreatePermissionRequest;
import com.blueant_crm_erp.permission.dto.request.PermissionSearchRequest;
import com.blueant_crm_erp.permission.dto.request.UpdatePermissionRequest;
import com.blueant_crm_erp.permission.dto.response.PermissionDropdownResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionSummaryResponse;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.mapper.PermissionMapper;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.permission.service.PermissionService;
import com.blueant_crm_erp.permission.specification.PermissionSpecification;
import com.blueant_crm_erp.permission.validator.PermissionValidator;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * Permission Service Implementation
 * =============================================================================
 *
 * Business implementation of Permission Management.
 *
 * Responsibilities
 * ----------------
 * • Create Permission
 * • Update Permission
 * • Delete Permission
 * • Restore Permission
 * • Search Permission
 * • Dropdown
 * • Statistics
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    private final RolePermissionRepository rolePermissionRepository;

    private final PermissionMapper permissionMapper;

    private final PermissionValidator permissionValidator;

    /**
     * =========================================================================
     * Create Permission
     * =========================================================================
     */
    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {

        permissionValidator.validateCreate(request);

        Permission permission = permissionMapper.toEntity(request);

        permission = permissionRepository.save(permission);

        return permissionMapper.toResponse(permission);
    }

    /**
     * =========================================================================
     * Update Permission
     * =========================================================================
     */
    @Override
    public PermissionResponse updatePermission(
            Long permissionId,
            UpdatePermissionRequest request) {

        Permission permission = permissionRepository
                .findByIdAndDeletedFalse(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        permissionValidator.validateUpdate(
                permissionId,
                request,
                permission
        );

        permissionMapper.updateEntityFromRequest(
                request,
                permission
        );

        permission = permissionRepository.save(permission);

        return permissionMapper.toResponse(permission);
    }

    /**
     * =========================================================================
     * Soft Delete Permission
     * =========================================================================
     */
    @Override
    public void deletePermission(Long permissionId) {

        Permission permission = permissionRepository
                .findByIdAndDeletedFalse(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        if (Boolean.TRUE.equals(permission.getSystemPermission())) {
            throw new IllegalArgumentException(
                    "System Permission cannot be deleted."
            );
        }

        if (isAssignedToAnyRole(permissionId)) {
            throw new IllegalArgumentException(
                    "Permission is assigned to one or more Roles."
            );
        }

        permission.setDeleted(Boolean.TRUE);
        permission.setDeletedAt(LocalDateTime.now());

        permissionRepository.save(permission);
    }

    /**
     * =========================================================================
     * Restore Permission
     * =========================================================================
     */
    @Override
    public void restorePermission(Long permissionId) {

        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        permission.setDeleted(Boolean.FALSE);
        permission.setDeletedAt(null);
        permission.setDeletedBy(null);

        permissionRepository.save(permission);
    }

    /**
     * =========================================================================
     * Change Permission Status
     * =========================================================================
     */
    @Override
    public PermissionResponse changePermissionStatus(
            Long permissionId,
            ChangePermissionStatusRequest request) {

        Permission permission = permissionRepository
                .findByIdAndDeletedFalse(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        permission.setStatus(request.getStatus());

        permission = permissionRepository.save(permission);

        return permissionMapper.toResponse(permission);
    }
    /**
     * =========================================================================
     * Get Permission By Id
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long permissionId) {

        Permission permission = permissionRepository
                .findByIdAndDeletedFalse(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        return permissionMapper.toResponse(permission);
    }

    /**
     * =========================================================================
     * Get Permission By Code
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByCode(String code) {

        Permission permission = permissionRepository
                .findByCodeIgnoreCase(code)
                .orElseThrow(() ->
                        new IllegalArgumentException("Permission not found."));

        if (Boolean.TRUE.equals(permission.getDeleted())) {
            throw new IllegalArgumentException("Permission not found.");
        }

        return permissionMapper.toResponse(permission);
    }

    /**
     * =========================================================================
     * Get All Active Permissions
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionSummaryResponse> getAllPermissions() {

        return permissionMapper.toSummaryResponseList(
                permissionRepository.findAllByDeletedFalseOrderByNameAsc()
        );
    }

    /**
     * =========================================================================
     * Get All Permissions Including Deleted
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionSummaryResponse> getAllPermissionsIncludingDeleted() {

        return permissionMapper.toSummaryResponseList(
                permissionRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "displayOrder")
                )
        );
    }

    /**
     * =========================================================================
     * Search Permissions
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionSummaryResponse> searchPermissions(
            PermissionSearchRequest request) {

        Sort.Direction direction =
                "DESC".equalsIgnoreCase(request.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, request.getSortBy())
        );

        Page<Permission> page = permissionRepository.findAll(
                PermissionSpecification.search(request),
                pageable
        );

        List<PermissionSummaryResponse> content =
                permissionMapper.toSummaryResponseList(
                        page.getContent()
                );

        return PageResponse.<PermissionSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .sort(page.getSort().toString())
                .build();
    }

    /**
     * =========================================================================
     * Permission Dropdown
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDropdownResponse> getPermissionDropdown() {

        return permissionMapper.toDropdownResponseList(
                permissionRepository.findAllByDeletedFalseOrderByNameAsc()
        );
    }

    /**
     * =========================================================================
     * Exists By Id
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long permissionId) {

        return permissionRepository.existsById(permissionId);
    }

    /**
     * =========================================================================
     * Exists By Code
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {

        return permissionRepository.existsByCodeIgnoreCase(code);
    }

    /**
     * =========================================================================
     * Exists By Name
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {

        return permissionRepository.existsByNameIgnoreCase(name);
    }

    /**
     * =========================================================================
     * Check Permission Assigned To Any Role
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAssignedToAnyRole(Long permissionId) {

        return rolePermissionRepository.existsByPermissionId(permissionId);
    }

    /**
     * =========================================================================
     * Total Permission Count
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public long countPermissions() {

        return permissionRepository.countByDeletedFalse();
    }

    /**
     * =========================================================================
     * Active Permission Count
     * =========================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public long countActivePermissions() {

        return permissionRepository
                .findAllByStatusAndDeletedFalse(Status.ACTIVE)
                .size();
    }

}