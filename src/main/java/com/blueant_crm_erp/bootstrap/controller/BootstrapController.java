package com.blueant_crm_erp.bootstrap.controller;

import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.bootstrap.dto.request.BootstrapRequest;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapExecutionReport;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapStatusResponse;
import com.blueant_crm_erp.bootstrap.service.BootstrapService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.SuccessResponse;
import com.blueant_crm_erp.exception.auth.UnauthorizedException;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * =============================================================================
 * Bootstrap Controller
 * =============================================================================
 *
 * REST Controller for Database Bootstrap operations.
 *
 * Provides dual-mode authorization:
 * 1. Authenticated Maintenance: Accessible with valid Super Admin JWT.
 * 2. Initial Recovery Mode: When users table is empty, accessible via X-Bootstrap-Secret header.
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

    private static final String BOOTSTRAP_SECRET_HEADER = "X-Bootstrap-Secret";

    private final BootstrapService bootstrapService;
    private final UserRepository userRepository;
    private final BootstrapProperties bootstrapProperties;

    /**
     * Trigger database bootstrap.
     */
    @Operation(summary = "Run Bootstrap", description = "Executes the database bootstrap process manually.")
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<BootstrapExecutionReport>> runBootstrap(
            @RequestHeader(value = BOOTSTRAP_SECRET_HEADER, required = false) String secret,
            @Valid @RequestBody(required = false) BootstrapRequest request) {

        boolean isSuperAdmin = isSuperAdminAuthenticated();

        if (!isSuperAdmin) {
            // Initial Recovery Mode: Verify secret and ensure system is not already initialized
            validateBootstrapSecret(secret);

            long userCount = userRepository.count();
            if (userCount > 0) {
                log.warn("Rejected unauthenticated bootstrap recovery: Users already exist in database (count={}).", userCount);
                throw new BadRequestException("Bootstrap rejected: System has already been initialized with existing users.");
            }
            log.info("Executing initial database bootstrap in recovery mode (users table is empty).");
        } else {
            log.info("Executing database bootstrap under authenticated Super Admin session.");
        }

        if (request == null) {
            request = BootstrapRequest.builder()
                    .force(false)
                    .masterData(true)
                    .securityData(true)
                    .users(true)
                    .build();
        } else {
            if (request.getForce() == null) request.setForce(false);
            if (request.getMasterData() == null) request.setMasterData(true);
            if (request.getSecurityData() == null) request.setSecurityData(true);
            if (request.getUsers() == null) request.setUsers(true);
        }

        BootstrapExecutionReport response = bootstrapService.bootstrap(request);

        return ResponseEntity.ok(SuccessResponse.success("Bootstrap executed successfully", response));
    }

    /**
     * Get bootstrap status.
     */
    @Operation(summary = "Get Bootstrap Status", description = "Retrieves the current status of the database bootstrap.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<BootstrapStatusResponse>> getBootstrapStatus(
            @RequestHeader(value = BOOTSTRAP_SECRET_HEADER, required = false) String secret) {

        if (!isSuperAdminAuthenticated()) {
            validateBootstrapSecret(secret);
        }

        log.info("Received request to fetch bootstrap status");
        BootstrapStatusResponse response = bootstrapService.getBootstrapStatus();

        return ResponseEntity.ok(SuccessResponse.success("Bootstrap status retrieved successfully", response));
    }

    /**
     * Validates whether current security context holds an authenticated Super Admin.
     */
    private boolean isSuperAdminAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority())
                || "BOOTSTRAP_EXECUTE".equalsIgnoreCase(a.getAuthority())
        );
    }

    /**
     * Constant-time verification of the bootstrap secret.
     */
    private void validateBootstrapSecret(String secret) {
        String configuredSecret = bootstrapProperties.getSecret();
        if (configuredSecret == null || configuredSecret.isBlank()) {
            log.error("Database bootstrap recovery is disabled: No bootstrap secret configured on server.");
            throw new UnauthorizedException("Bootstrap recovery is disabled or unconfigured.");
        }
        if (secret == null || secret.isBlank()) {
            throw new UnauthorizedException("Missing required " + BOOTSTRAP_SECRET_HEADER + " header.");
        }
        byte[] providedBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes = configuredSecret.trim().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(providedBytes, expectedBytes)) {
            throw new UnauthorizedException("Invalid bootstrap secret.");
        }
    }

}
