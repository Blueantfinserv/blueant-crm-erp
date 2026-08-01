package com.blueant_crm_erp.role.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.role.constant.RoleConstants;
import com.blueant_crm_erp.role.dto.request.AssignPermissionRequest;
import com.blueant_crm_erp.role.dto.request.ChangeRoleStatusRequest;
import com.blueant_crm_erp.role.dto.request.CreateRoleRequest;
import com.blueant_crm_erp.role.dto.request.UpdateRoleRequest;
import com.blueant_crm_erp.role.dto.response.RolePermissionResponse;
import com.blueant_crm_erp.role.dto.response.RoleResponse;
import com.blueant_crm_erp.role.dto.response.RoleSummaryResponse;
import com.blueant_crm_erp.role.service.RolePermissionService;
import com.blueant_crm_erp.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =============================================================================
 * Role Controller
 * =============================================================================
 *
 * REST APIs for Role Management.
 *
 * Responsibilities:
 * - Create Role
 * - Update Role
 * - Get Role
 * - Get All Roles
 * - Search Roles
 * - Change Role Status
 * - Delete Role
 * - Assign Permissions
 * - Remove Permissions
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 * =============================================================================
 */
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Role Management",
        description = "APIs for managing roles"
)
@RequestMapping(RoleConstants.API_BASE)
public class RoleController {

    private final RoleService roleService;

    private final RolePermissionService rolePermissionService;

    /**
     * Create Role
     */
    @PostMapping
    @Operation(
            summary     = "Create New Role",
            description = "Creates a new role with the specified information."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate Resource")
    })
    public ApiResponse<RoleResponse> createRole(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody CreateRoleRequest request) {

        return ApiResponse.success(
                roleService.createRole(request)
        );
    }

    /**
     * Update Role
     */
    @PutMapping(RoleConstants.ROLE_ID)
    @Operation(
            summary     = "Update Existing Role",
            description = "Updates an existing role with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate Resource")
    })
    public ApiResponse<RoleResponse> updateRole(
            @Parameter(description = "ID of the role") @PathVariable Long roleId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateRoleRequest request) {

        return ApiResponse.success(
                roleService.updateRole(roleId, request)
        );
    }

    /**
     * Get Role By Id
     */
    @GetMapping(RoleConstants.ROLE_ID)
    @Operation(
            summary     = "Get Role By ID",
            description = "Retrieves role details by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<RoleResponse> getRoleById(
            @Parameter(description = "ID of the role") @PathVariable Long roleId) {

        return ApiResponse.success(
                roleService.getRoleById(roleId)
        );
    }

    /**
     * Get All Roles
     */
    @GetMapping
    @Operation(
            summary     = "Get All Roles",
            description = "Retrieves a list of all available roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<List<RoleSummaryResponse>> getAllRoles() {

        return ApiResponse.success(
                roleService.getAllRoles()
        );
    }

    /**
     * Get Roles With Pagination
     */
    @GetMapping(RoleConstants.PAGE)
    @Operation(
            summary     = "Get Paginated Roles",
            description = "Retrieves a paginated list of roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<PageResponse<RoleSummaryResponse>> getAllRoles(
            Pageable pageable) {

        return ApiResponse.success(
                roleService.getAllRoles(pageable)
        );
    }

    /**
     * Search Roles
     */
    @GetMapping(RoleConstants.SEARCH)
    @Operation(
            summary     = "Search Roles",
            description = "Searches for roles based on a specific keyword."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed")
    })
    public ApiResponse<PageResponse<RoleSummaryResponse>> searchRoles(
            @RequestParam String keyword,
            Pageable pageable) {

        return ApiResponse.success(
                roleService.searchRoles(keyword, pageable)
        );
    }

    /**
     * Change Role Status
     */
    @PatchMapping(RoleConstants.STATUS)
    @Operation(
            summary     = "Change Role Status",
            description = "Changes the active status of a specific role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<RoleResponse> changeRoleStatus(
            @Parameter(description = "ID of the role") @PathVariable Long roleId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangeRoleStatusRequest request) {

        return ApiResponse.success(
                roleService.changeRoleStatus(roleId, request)
        );
    }

    /**
     * Delete Role
     */
    @DeleteMapping(RoleConstants.ROLE_ID)
    @Operation(
            summary     = "Delete Role",
            description = "Deletes a specific role by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<String> deleteRole(
            @Parameter(description = "ID of the role") @PathVariable Long roleId) {

        roleService.deleteRole(roleId);

        return ApiResponse.success(
                "Role deleted successfully."
        );
    }

    /**
     * Assign Permissions
     */
    @PostMapping(RoleConstants.PERMISSIONS)
    @Operation(
            summary     = "Assign Permissions To Role",
            description = "Assigns a set of permissions to a specific role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<List<RolePermissionResponse>> assignPermissions(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody AssignPermissionRequest request) {

        return ApiResponse.success(
                rolePermissionService.assignPermissions(request)
        );
    }

    /**
     * Remove Permissions
     */
    @DeleteMapping(RoleConstants.PERMISSIONS)
    @Operation(
            summary     = "Remove Permissions From Role",
            description = "Removes a set of permissions from a specific role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<String> removePermissions(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody AssignPermissionRequest request) {

        rolePermissionService.removePermissions(request);

        return ApiResponse.success(
                "Permissions removed successfully."
        );
    }

    /**
     * Get Permissions By Role
     */
    @GetMapping(RoleConstants.ROLE_PERMISSIONS)
    @Operation(
            summary     = "Get Permissions Assigned To Role",
            description = "Retrieves all permissions currently assigned to a role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<List<RolePermissionResponse>> getPermissionsByRole(
            @Parameter(description = "ID of the role") @PathVariable Long roleId) {

        return ApiResponse.success(
                rolePermissionService.getPermissionsByRoleId(roleId)
        );
    }
}
