package com.blueant_crm_erp.bootstrap.controller;

import com.blueant_crm_erp.bootstrap.dto.request.BootstrapRequest;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapExecutionReport;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapStatusResponse;
import com.blueant_crm_erp.bootstrap.service.BootstrapService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * =============================================================================
 * Bootstrap Controller
 * =============================================================================
 *
 * REST Controller for Database Bootstrap operations.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@RestController
@RequestMapping("/bootstrap")
@RequiredArgsConstructor
@Tag(name = "Bootstrap", description = "Bootstrap Management API")
public class BootstrapController {

    private final BootstrapService bootstrapService;

    /**
     * Trigger manual bootstrap.
     */
    @Operation(summary = "Run Bootstrap", description = "Executes the database bootstrap process manually.")
    @PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('BOOTSTRAP_EXECUTE')")
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<BootstrapExecutionReport>> runBootstrap(
            @Valid @RequestBody BootstrapRequest request) {

        log.info("Received request to run database bootstrap: Force={}, MasterData={}, Security={}, Users={}",
                request.getForce(), request.getMasterData(), request.getSecurityData(), request.getUsers());

        BootstrapExecutionReport response = bootstrapService.bootstrap(request);

        return ResponseEntity.ok(SuccessResponse.success("Bootstrap executed successfully", response));
    }

    /**
     * Get bootstrap status.
     */
    @Operation(summary = "Get Bootstrap Status", description = "Retrieves the current status of the database bootstrap.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<BootstrapStatusResponse>> getBootstrapStatus() {
        
        log.info("Received request to fetch bootstrap status");
        
        BootstrapStatusResponse response = bootstrapService.getBootstrapStatus();

        return ResponseEntity.ok(SuccessResponse.success("Bootstrap status retrieved successfully", response));
    }

}
