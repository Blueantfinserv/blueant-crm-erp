package com.blueant_crm_erp.user.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.constant.UserConstants;
import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.ChangeUserStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateUserRequest;
import com.blueant_crm_erp.user.dto.request.ResetPasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserRequest;
import com.blueant_crm_erp.user.dto.request.UserSearchRequest;
import com.blueant_crm_erp.user.dto.response.UserDropdownResponse;
import com.blueant_crm_erp.user.dto.response.UserResponse;
import com.blueant_crm_erp.user.dto.response.UserSummaryResponse;
import com.blueant_crm_erp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(UserConstants.API_BASE)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    // =========================================================================
    // Create
    // =========================================================================

    @PostMapping
    @Operation(
            summary     = "Create New User",
            description = "Creates a new user and validates uniqueness constraints."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate field"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role, department, designation, or team not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody CreateUserRequest request) {
        log.info("REST request to create user: {}", request.getEmployeeCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.createUser(request)));
    }

    // =========================================================================
    // Update
    // =========================================================================

    @PutMapping(UserConstants.USER_ID)
    @Operation(
            summary     = "Update Existing User",
            description = "Updates an existing user with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate field"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or referenced entity not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long userId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateUserRequest request) {
        log.info("REST request to update user with id: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, request)));
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @DeleteMapping(UserConstants.USER_ID)
    @Operation(
            summary     = "Delete User",
            description = "Marks a specific user as deleted."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted — no content returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long userId) {
        log.info("REST request to delete user with id: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(UserConstants.USER_ID + "/restore")
    @Operation(
            summary     = "Restore Deleted User",
            description = "Restores a previously deleted user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deleted user not found")
    })
    public ResponseEntity<ApiResponse<String>> restoreUser(
            @Parameter(description = "ID of the user to restore") @PathVariable Long userId) {
        log.info("REST request to restore user with id: {}", userId);
        userService.restoreUser(userId);
        return ResponseEntity.ok(ApiResponse.success(UserConstants.USER_RESTORED));
    }

    // =========================================================================
    // Status
    // =========================================================================

    @PatchMapping(UserConstants.USER_ID + UserConstants.STATUS)
    @Operation(
            summary     = "Change User Status",
            description = "Changes the active status of a specific user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User status changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangeUserStatusRequest request) {
        log.info("REST request to change status of user with id: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(
                UserConstants.USER_STATUS_UPDATED, userService.changeUserStatus(userId, request)));
    }

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping(UserConstants.USER_ID)
    @Operation(
            summary     = "Get User By ID",
            description = "Retrieves user details by their unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long userId) {
        log.info("REST request to get user with id: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @GetMapping("/employee-code/{employeeCode}")
    @Operation(
            summary     = "Get User By Employee Code",
            description = "Retrieves user details by their employee code."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmployeeCode(
            @Parameter(description = "Employee code of the user to retrieve") @PathVariable String employeeCode) {
        log.info("REST request to get user with employeeCode: {}", employeeCode);
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmployeeCode(employeeCode)));
    }

    @PostMapping(UserConstants.SEARCH)
    @Operation(
            summary     = "Search Users",
            description = "Searches for users based on specific criteria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryResponse>>> searchUsers(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UserSearchRequest request) {
        log.info("REST request to search users");
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(request)));
    }

    @GetMapping(UserConstants.DROPDOWN)
    @Operation(
            summary     = "Get User Dropdown",
            description = "Retrieves a lightweight list of users formatted for dropdowns."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dropdown list returned")
    })
    public ResponseEntity<ApiResponse<List<UserDropdownResponse>>> getUserDropdown() {
        log.info("REST request to get user dropdown");
        return ResponseEntity.ok(ApiResponse.success(userService.getUserDropdown()));
    }

    // =========================================================================
    // Password Management
    // =========================================================================

    @PostMapping(UserConstants.USER_ID + UserConstants.RESET_PASSWORD)
    @Operation(
            summary     = "Reset User Password",
            description = "Resets the password for a specific user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "New password is same as old password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Parameter(description = "ID of the user whose password to reset") @PathVariable Long userId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ResetPasswordRequest request) {
        log.info("REST request to reset password for user with id: {}", userId);
        userService.resetPassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(UserConstants.PASSWORD_RESET));
    }

    @PostMapping(UserConstants.USER_ID + UserConstants.CHANGE_PASSWORD)
    @Operation(
            summary     = "Change User Password",
            description = "Changes the password for a specific user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Current password mismatch or new password same as old"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangePasswordRequest request) {
        log.info("REST request to change password for user with id: {}", userId);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(UserConstants.PASSWORD_CHANGED));
    }
}
