package com.blueant_crm_erp.permission.validator;

import com.blueant_crm_erp.permission.dto.request.CreatePermissionRequest;
import com.blueant_crm_erp.permission.dto.request.UpdatePermissionRequest;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * =============================================================================
 * Permission Validator
 * =============================================================================
 *
 * Performs all business validations related to Permission Management.
 *
 * Responsibilities:
 * - Validate Create Permission
 * - Validate Update Permission
 * - Validate Permission Existence
 * - Validate Permission Id
 * - Validate Permission Name
 * - Validate Permission Code
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionRepository permissionRepository;

    /**
     * Validate Create Permission
     */
    public void validateCreate(CreatePermissionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Permission request cannot be null.");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("Permission name is required.");
        }

        if (!StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("Permission code is required.");
        }

        if (!StringUtils.hasText(request.getModule())) {
            throw new IllegalArgumentException("Module name is required.");
        }

        if (permissionRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Permission name already exists.");
        }

        if (permissionRepository.existsByCodeIgnoreCaseAndDeletedFalse(request.getCode())) {
            throw new IllegalArgumentException("Permission code already exists.");
        }
    }

    /**
     * Validate Update Permission
     */
    public void validateUpdate(Long permissionId,
                               UpdatePermissionRequest request,
                               Permission permission) {

        validatePermissionId(permissionId);

        if (request == null) {
            throw new IllegalArgumentException("Update request cannot be null.");
        }

        validatePermissionExists(permission);

        if (StringUtils.hasText(request.getName())
                && !permission.getName().equalsIgnoreCase(request.getName())
                && permissionRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {

            throw new IllegalArgumentException("Permission name already exists.");
        }

        if (StringUtils.hasText(request.getCode())
                && !permission.getCode().equalsIgnoreCase(request.getCode())
                && permissionRepository.existsByCodeIgnoreCaseAndDeletedFalse(request.getCode())) {

            throw new IllegalArgumentException("Permission code already exists.");
        }
    }

    /**
     * Validate Permission Exists
     */
    public void validatePermissionExists(Permission permission) {

        if (permission == null) {
            throw new IllegalArgumentException("Permission not found.");
        }

        if (Boolean.TRUE.equals(permission.getDeleted())) {
            throw new IllegalArgumentException("Permission has been deleted.");
        }
    }

    /**
     * Validate Permission Id
     */
    public void validatePermissionId(Long permissionId) {

        if (permissionId == null) {
            throw new IllegalArgumentException("Permission Id is required.");
        }
    }

    /**
     * Validate Permission Name
     */
    public void validatePermissionName(String name) {

        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Permission name is required.");
        }
    }

    /**
     * Validate Permission Code
     */
    public void validatePermissionCode(String code) {

        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("Permission code is required.");
        }
    }

    /**
     * Validate Module Name
     */
    public void validateModule(String module) {

        if (!StringUtils.hasText(module)) {
            throw new IllegalArgumentException("Module name is required.");
        }
    }
}