package com.blueant_crm_erp.permission.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.permission.constant.PermissionConstants;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.permission.dto.request.PermissionSearchRequest;
import com.blueant_crm_erp.permission.dto.response.PermissionSummaryResponse;
import com.blueant_crm_erp.permission.dto.request.ChangePermissionStatusRequest;
import com.blueant_crm_erp.permission.dto.request.CreatePermissionRequest;
import com.blueant_crm_erp.permission.dto.request.UpdatePermissionRequest;
import com.blueant_crm_erp.permission.dto.response.PermissionDropdownResponse;
import com.blueant_crm_erp.permission.dto.response.PermissionResponse;
import com.blueant_crm_erp.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =============================================================================
 * Permission Controller
 * =============================================================================
 *
 * REST APIs for Permission Management.
 *
 * Responsibilities
 * ----------------
 * • Create Permission
 * • Update Permission
 * • Delete Permission
 * • Restore Permission
 * • Change Permission Status
 * • Get Permission By Id
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */

@RequiredArgsConstructor
@Tag(
        name = "Permission Management",
        description = "APIs for managing permissions"
)
@RestController
@RequestMapping(PermissionConstants.API_BASE)
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * =========================================================================
     * Create Permission
     * =========================================================================
     */
    @PostMapping
    @Operation(
            summary     = "Create New Permission",
            description = "Creates a new permission with the specified information."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate Resource")
    })
    public ApiResponse<PermissionResponse> createPermission(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody
            CreatePermissionRequest request) {

        return ApiResponse.success(
                permissionService.createPermission(request)
        );
    }

    /**
     * =========================================================================
     * Update Permission
     * =========================================================================
     */
    @PutMapping(PermissionConstants.PERMISSION_ID)
    @Operation(
            summary     = "Update Existing Permission",
            description = "Updates an existing permission with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate Resource")
    })
    public ApiResponse<PermissionResponse> updatePermission(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody
            UpdatePermissionRequest request) {

        return ApiResponse.success(
                permissionService.updatePermission(
                        permissionId,
                        request
                )
        );
    }

    /**
     * =========================================================================
     * Get Permission By Id
     * =========================================================================
     */
    @GetMapping(PermissionConstants.PERMISSION_ID)
    @Operation(
            summary     = "Get Permission By ID",
            description = "Retrieves permission details by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<PermissionResponse> getPermissionById(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId) {

        return ApiResponse.success(
                permissionService.getPermissionById(permissionId)
        );
    }

    /**
     * =========================================================================
     * Change Permission Status
     * =========================================================================
     */
    @PatchMapping(PermissionConstants.STATUS)
    @Operation(
            summary     = "Change Permission Status",
            description = "Changes the active status of a specific permission."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<PermissionResponse> changePermissionStatus(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody
            ChangePermissionStatusRequest request) {

        return ApiResponse.success(
                permissionService.changePermissionStatus(
                        permissionId,
                        request
                )
        );
    }



    /**
     * =========================================================================
     * Delete Permission
     * =========================================================================
     */
    @DeleteMapping(PermissionConstants.PERMISSION_ID)
    @Operation(
            summary     = "Delete Permission",
            description = "Deletes a specific permission by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<String> deletePermission(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId) {

        permissionService.deletePermission(permissionId);

        return ApiResponse.success(
                PermissionConstants.PERMISSION_DELETED_SUCCESS
        );
    }

    /**
     * =========================================================================
     * Restore Permission
     * =========================================================================
     */
    @PatchMapping("/{permissionId}/restore")
    @Operation(
            summary     = "Restore Deleted Permission",
            description = "Restores a previously deleted permission."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<String> restorePermission(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId) {

        permissionService.restorePermission(permissionId);

        return ApiResponse.success(
                "Permission restored successfully."
        );
    }
    /**
     * =========================================================================
     * Get Permission By Code
     * =========================================================================
     */
    @GetMapping("/code/{code}")
    @Operation(
            summary     = "Get Permission By Code",
            description = "Retrieves permission details by its unique code."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    public ApiResponse<PermissionResponse> getPermissionByCode(

            @Parameter(description = "Code of the permission") @PathVariable
            String code) {

        return ApiResponse.success(
                permissionService.getPermissionByCode(code)
        );
    }

    /**
     * =========================================================================
     * Get All Active Permissions
     * =========================================================================
     */
    @GetMapping
    @Operation(
            summary     = "Get All Active Permissions",
            description = "Retrieves a list of all active permissions."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<List<PermissionSummaryResponse>> getAllPermissions() {

        return ApiResponse.success(
                permissionService.getAllPermissions()
        );
    }

    /**
     * =========================================================================
     * Get All Permissions Including Deleted
     * =========================================================================
     */
    @GetMapping("/all")
    @Operation(
            summary     = "Get All Permissions Including Deleted",
            description = "Retrieves a list of all permissions, including deleted ones."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<List<PermissionSummaryResponse>>
    getAllPermissionsIncludingDeleted() {

        return ApiResponse.success(
                permissionService.getAllPermissionsIncludingDeleted()
        );
    }

    /**
     * =========================================================================
     * Search Permissions
     * =========================================================================
     */
    @PostMapping(PermissionConstants.SEARCH)
    @Operation(
            summary     = "Search Permissions",
            description = "Searches for permissions based on specific criteria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation Failed")
    })
    public ApiResponse<PageResponse<PermissionSummaryResponse>>
    searchPermissions(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody
            PermissionSearchRequest request) {

        return ApiResponse.success(
                permissionService.searchPermissions(request)
        );
    }

    /**
     * =========================================================================
     * Permission Dropdown
     * =========================================================================
     */
    @GetMapping(PermissionConstants.DROPDOWN)
    @Operation(
            summary     = "Get Permission Dropdown",
            description = "Retrieves a lightweight list of permissions formatted for dropdowns."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<List<PermissionDropdownResponse>>
    getPermissionDropdown() {

        return ApiResponse.success(
                permissionService.getPermissionDropdown()
        );
    }

    /**
     * =========================================================================
     * Total Permission Count
     * =========================================================================
     */
    @GetMapping("/count")
    @Operation(
            summary     = "Get Total Permission Count",
            description = "Retrieves the total number of permissions in the system."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Long> countPermissions() {

        return ApiResponse.success(
                permissionService.countPermissions()
        );
    }

    /**
     * =========================================================================
     * Active Permission Count
     * =========================================================================
     */
    @GetMapping("/count/active")
    @Operation(
            summary     = "Get Active Permission Count",
            description = "Retrieves the total number of active permissions in the system."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Long> countActivePermissions() {

        return ApiResponse.success(
                permissionService.countActivePermissions()
        );
    }

    /**
     * =========================================================================
     * Check Permission Exists
     * =========================================================================
     */
    @GetMapping("/{permissionId}/exists")
    @Operation(
            summary     = "Check Permission Exists By ID",
            description = "Checks whether a permission exists by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Boolean> existsById(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId) {

        return ApiResponse.success(
                permissionService.existsById(permissionId)
        );
    }

    /**
     * =========================================================================
     * Check Permission Exists By Code
     * =========================================================================
     */
    @GetMapping("/exists/code/{code}")
    @Operation(
            summary     = "Check Permission Exists By Code",
            description = "Checks whether a permission exists by its unique code."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Boolean> existsByCode(

            @Parameter(description = "Code of the permission") @PathVariable
            String code) {

        return ApiResponse.success(
                permissionService.existsByCode(code)
        );
    }

    /**
     * =========================================================================
     * Check Permission Exists By Name
     * =========================================================================
     */
    @GetMapping("/exists/name/{name}")
    @Operation(
            summary     = "Check Permission Exists By Name",
            description = "Checks whether a permission exists by its name."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Boolean> existsByName(

            @Parameter(description = "Name of the permission") @PathVariable
            String name) {

        return ApiResponse.success(
                permissionService.existsByName(name)
        );
    }

    /**
     * =========================================================================
     * Check Permission Assigned To Any Role
     * =========================================================================
     */
    @GetMapping("/{permissionId}/assigned")
    @Operation(
            summary     = "Check Permission Assigned To Role",
            description = "Checks whether a specific permission is currently assigned to any role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ApiResponse<Boolean> isAssignedToAnyRole(

            @Parameter(description = "ID of the permission") @PathVariable
            Long permissionId) {

        return ApiResponse.success(
                permissionService.isAssignedToAnyRole(permissionId)
        );
    }

}

