package com.blueant_crm_erp.user.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.user.constant.DesignationConstants;
import com.blueant_crm_erp.user.dto.request.ChangeDesignationStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDesignationRequest;
import com.blueant_crm_erp.user.dto.request.DesignationSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDesignationRequest;
import com.blueant_crm_erp.user.dto.response.DesignationDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DesignationResponse;
import com.blueant_crm_erp.user.dto.response.DesignationSummaryResponse;
import com.blueant_crm_erp.user.service.DesignationService;
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
@RequestMapping(DesignationConstants.API_BASE)
@RequiredArgsConstructor
@Tag(name = "Designation Management", description = "APIs for managing designations")
public class DesignationController {

    private final DesignationService designationService;

    // =========================================================================
    // Create
    // =========================================================================

    @PostMapping
    @Operation(
            summary     = "Create New Designation",
            description = "Creates a new designation and validates code and name uniqueness."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Designation created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name")
    })
    public ResponseEntity<ApiResponse<DesignationResponse>> createDesignation(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody CreateDesignationRequest request) {
        log.info("REST request to create designation with code: {}", request.getCode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(designationService.createDesignation(request)));
    }

    // =========================================================================
    // Update
    // =========================================================================

    @PutMapping(DesignationConstants.DESIGNATION_ID)
    @Operation(
            summary     = "Update Existing Designation",
            description = "Updates an existing designation with the provided details."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate code / name"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<DesignationResponse>> updateDesignation(
            @Parameter(description = "ID of the designation to update") @PathVariable Long designationId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateDesignationRequest request) {
        log.info("REST request to update designation with id: {}", designationId);
        return ResponseEntity.ok(ApiResponse.success(
                designationService.updateDesignation(designationId, request)));
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @DeleteMapping(DesignationConstants.DESIGNATION_ID)
    @Operation(
            summary     = "Delete Designation",
            description = "Marks a specific designation as deleted if no active entities are assigned."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Designation deleted — no content returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Designation has assigned users"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteDesignation(
            @Parameter(description = "ID of the designation to delete") @PathVariable Long designationId) {
        log.info("REST request to delete designation with id: {}", designationId);
        designationService.deleteDesignation(designationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(DesignationConstants.RESTORE)
    @Operation(
            summary     = "Restore Deleted Designation",
            description = "Restores a previously deleted designation."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deleted designation not found")
    })
    public ResponseEntity<ApiResponse<String>> restoreDesignation(
            @Parameter(description = "ID of the designation to restore") @PathVariable Long designationId) {
        log.info("REST request to restore designation with id: {}", designationId);
        designationService.restoreDesignation(designationId);
        return ResponseEntity.ok(ApiResponse.success("Designation restored successfully."));
    }

    // =========================================================================
    // Status
    // =========================================================================

    @PatchMapping(DesignationConstants.STATUS)
    @Operation(
            summary     = "Change Designation Status",
            description = "Changes the active status of a specific designation."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<DesignationResponse>> changeDesignationStatus(
            @Parameter(description = "ID of the designation") @PathVariable Long designationId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangeDesignationStatusRequest request) {
        log.info("REST request to change status of designation with id: {}", designationId);
        return ResponseEntity.ok(ApiResponse.success(
                designationService.changeDesignationStatus(designationId, request)));
    }

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping(DesignationConstants.DESIGNATION_ID)
    @Operation(
            summary     = "Get Designation By ID",
            description = "Retrieves designation details by its unique identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<DesignationResponse>> getDesignationById(
            @Parameter(description = "ID of the designation to retrieve") @PathVariable Long designationId) {
        log.info("REST request to get designation with id: {}", designationId);
        return ResponseEntity.ok(ApiResponse.success(
                designationService.getDesignationById(designationId)));
    }

    @GetMapping(DesignationConstants.NAME)
    @Operation(
            summary     = "Get Designation By Name",
            description = "Retrieves designation details by its name."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<DesignationResponse>> getDesignationByName(
            @Parameter(description = "Name of the designation to retrieve") @PathVariable String designationName) {
        log.info("REST request to get designation with name: {}", designationName);
        return ResponseEntity.ok(ApiResponse.success(
                designationService.getDesignationByName(designationName)));
    }

    @GetMapping
    @Operation(
            summary     = "Get All Active Designations",
            description = "Retrieves a list of all active designations."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation list returned")
    })
    public ResponseEntity<ApiResponse<List<DesignationSummaryResponse>>> getAllDesignations() {
        log.info("REST request to get all designations");
        return ResponseEntity.ok(ApiResponse.success(designationService.getAllDesignations()));
    }

    @GetMapping(DesignationConstants.ALL)
    @Operation(
            summary     = "Get All Designations Including Deleted",
            description = "Retrieves a list of all designations including deleted ones."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation list returned")
    })
    public ResponseEntity<ApiResponse<List<DesignationSummaryResponse>>> getAllDesignationsIncludingDeleted() {
        log.info("REST request to get all designations including deleted");
        return ResponseEntity.ok(ApiResponse.success(
                designationService.getAllDesignationsIncludingDeleted()));
    }

    @PostMapping(DesignationConstants.SEARCH)
    @Operation(
            summary     = "Search Designations",
            description = "Searches for designations based on specific criteria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<PageResponse<DesignationSummaryResponse>>> searchDesignations(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody DesignationSearchRequest request) {
        log.info("REST request to search designations");
        return ResponseEntity.ok(ApiResponse.success(designationService.searchDesignations(request)));
    }

    @GetMapping(DesignationConstants.DROPDOWN)
    @Operation(
            summary     = "Get Designation Dropdown",
            description = "Retrieves a lightweight list of designations formatted for dropdowns."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dropdown list returned")
    })
    public ResponseEntity<ApiResponse<List<DesignationDropdownResponse>>> getDesignationDropdown() {
        log.info("REST request to get designation dropdown");
        return ResponseEntity.ok(ApiResponse.success(designationService.getDesignationDropdown()));
    }
}
