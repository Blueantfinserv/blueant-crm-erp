package com.blueant_crm_erp.user.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.ChangeUserStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateUserRequest;
import com.blueant_crm_erp.user.dto.request.ResetPasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserRequest;
import com.blueant_crm_erp.user.dto.request.UserSearchRequest;
import com.blueant_crm_erp.user.dto.response.UserDropdownResponse;
import com.blueant_crm_erp.user.dto.response.UserResponse;
import com.blueant_crm_erp.user.dto.response.UserSummaryResponse;

import java.util.List;

/**
 * =============================================================================
 * User Service
 * =============================================================================
 *
 * Business operations for User Management.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy:
 * Rohit -> Sales Manager -> Team Leader -> Sales Person
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface UserService {

    /**
     * Create User
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Update User
     */
    UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    );

    /**
     * Soft Delete User
     */
    void deleteUser(Long userId);

    /**
     * Restore Deleted User
     */
    void restoreUser(Long userId);

    /**
     * Change User Status
     */
    UserResponse changeUserStatus(
            Long userId,
            ChangeUserStatusRequest request
    );

    /**
     * Get User By Id
     */
    UserResponse getUserById(Long userId);

    /**
     * Get User By Employee Code
     */
    UserResponse getUserByEmployeeCode(String employeeCode);

    /**
     * Search Users
     */
    PageResponse<UserSummaryResponse> searchUsers(
            UserSearchRequest request
    );

    /**
     * User Dropdown
     */
    List<UserDropdownResponse> getUserDropdown();

    /**
     * Reset Password
     */
    void resetPassword(
            Long userId,
            ResetPasswordRequest request
    );

    /**
     * Change Password
     */
    void changePassword(
            Long userId,
            ChangePasswordRequest request
    );

    /**
     * Check User Exists
     */
    boolean existsById(Long userId);

    /**
     * Check Employee Code Exists
     */
    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Check Email Exists
     */
    boolean existsByEmail(String email);

    /**
     * Check Mobile Exists
     */
    boolean existsByMobile(String mobile);

    /**
     * Total User Count
     */
    long countUsers();

    /**
     * Active User Count
     */
    long countActiveUsers();

}