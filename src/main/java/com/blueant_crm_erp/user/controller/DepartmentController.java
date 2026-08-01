package com.blueant_crm_erp.user.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.constant.DepartmentConstants;
import com.blueant_crm_erp.user.dto.request.ChangeDepartmentStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDepartmentRequest;
import com.blueant_crm_erp.user.dto.request.DepartmentSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDepartmentRequest;
import com.blueant_crm_erp.user.dto.response.DepartmentDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentResponse;
import com.blueant_crm_erp.user.dto.response.DepartmentSummaryResponse;
import com.blueant_crm_erp.user.service.DepartmentService;
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
@RequestMapping(DepartmentConstants.API_BASE)
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    // =========================================================================
    // Create
    // =========================================================================

    @PostMapping
    @Operation(
            summary     = "Create New Department",
            description = "Creates a new department and validates code and name uniqueness."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Department created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody CreateDepartmentRequest request) {
        log.info("REST request to create department with code: {}", request.getCode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(departmentService.createDepartment(request)));
    }

    // =========================================================================
    // Update
    // =========================================================================

    @PutMapping(DepartmentConstants.DEPARTMENT_ID)
    @Operation(
            summary     = "Update Existing Department",
            description = "Updates an existing department with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @Parameter(description = "ID of the department to update") @PathVariable Long departmentId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateDepartmentRequest request) {
        log.info("REST request to update department with id: {}", departmentId);
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.updateDepartment(departmentId, request)));
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @DeleteMapping(DepartmentConstants.DEPARTMENT_ID)
    @Operation(
            summary     = "Delete Department",
            description = "Marks a specific department as deleted if no active entities are assigned."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Department deleted — no content returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Department has assigned users, designations, or teams"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteDepartment(
            @Parameter(description = "ID of the department to delete") @PathVariable Long departmentId) {
        log.info("REST request to delete department with id: {}", departmentId);
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(DepartmentConstants.RESTORE)
    @Operation(
            summary     = "Restore Deleted Department",
            description = "Restores a previously deleted department."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deleted department not found")
    })
    public ResponseEntity<ApiResponse<String>> restoreDepartment(
            @Parameter(description = "ID of the department to restore") @PathVariable Long departmentId) {
        log.info("REST request to restore department with id: {}", departmentId);
        departmentService.restoreDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department restored successfully."));
    }

    // =========================================================================
    // Status
    // =========================================================================

    @PatchMapping(DepartmentConstants.STATUS)
    @Operation(
            summary     = "Change Department Status",
            description = "Changes the active status of a specific department."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> changeDepartmentStatus(
            @Parameter(description = "ID of the department") @PathVariable Long departmentId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangeDepartmentStatusRequest request) {
        log.info("REST request to change status of department with id: {}", departmentId);
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.changeDepartmentStatus(departmentId, request)));
    }

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping(DepartmentConstants.DEPARTMENT_ID)
    @Operation(
            summary     = "Get Department By ID",
            description = "Retrieves department details by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "ID of the department to retrieve") @PathVariable Long departmentId) {
        log.info("REST request to get department with id: {}", departmentId);
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.getDepartmentById(departmentId)));
    }

    @GetMapping(DepartmentConstants.NAME)
    @Operation(
            summary     = "Get Department By Name",
            description = "Retrieves department details by its name."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentByName(
            @Parameter(description = "Name of the department to retrieve") @PathVariable String departmentName) {
        log.info("REST request to get department with name: {}", departmentName);
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.getDepartmentByName(departmentName)));
    }

    @GetMapping
    @Operation(
            summary     = "Get All Active Departments",
            description = "Retrieves a list of all active departments."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department list returned")
    })
    public ResponseEntity<ApiResponse<List<DepartmentSummaryResponse>>> getAllDepartments() {
        log.info("REST request to get all departments");
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    @GetMapping(DepartmentConstants.ALL)
    @Operation(
            summary     = "Get All Departments Including Deleted",
            description = "Retrieves a list of all departments including deleted ones."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department list returned")
    })
    public ResponseEntity<ApiResponse<List<DepartmentSummaryResponse>>> getAllDepartmentsIncludingDeleted() {
        log.info("REST request to get all departments including deleted");
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.getAllDepartmentsIncludingDeleted()));
    }

    @PostMapping(DepartmentConstants.SEARCH)
    @Operation(
            summary     = "Search Departments",
            description = "Searches for departments based on specific criteria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<PageResponse<DepartmentSummaryResponse>>> searchDepartments(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody DepartmentSearchRequest request) {
        log.info("REST request to search departments");
        return ResponseEntity.ok(ApiResponse.success(departmentService.searchDepartments(request)));
    }

    @GetMapping(DepartmentConstants.DROPDOWN)
    @Operation(
            summary     = "Get Department Dropdown",
            description = "Retrieves a lightweight list of departments formatted for dropdowns."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dropdown list returned")
    })
    public ResponseEntity<ApiResponse<List<DepartmentDropdownResponse>>> getDepartmentDropdown() {
        log.info("REST request to get department dropdown");
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentDropdown()));
    }
}
