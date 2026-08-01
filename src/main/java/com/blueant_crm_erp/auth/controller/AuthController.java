package com.blueant_crm_erp.auth.controller;

import com.blueant_crm_erp.auth.dto.request.*;
import com.blueant_crm_erp.auth.dto.response.*;
import com.blueant_crm_erp.auth.service.AuthService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.common.dto.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * Authentication Controller
 * ============================================================================
 *
 * REST APIs for Authentication & Authorization.
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * ============================================================================
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Session Management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login to the system", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Login successful", "/auth/login", response
        ));
    }

    @PostMapping("/verify-login-otp")
    @Operation(summary = "Verify Login OTP", description = "Verifies MFA OTP during login")
    public ResponseEntity<SuccessResponse<LoginResponse>> verifyLoginOtp(@Valid @RequestBody VerifyLoginOtpRequest request) {
        LoginResponse response = authService.verifyLoginOtp(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "OTP Verified successfully", "/auth/verify-login-otp", response
        ));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT Token", description = "Generates new access token from refresh token")
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Token refreshed successfully", "/auth/refresh-token", response
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current session", description = "Revokes refresh token and clears session")
    public ResponseEntity<SuccessResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Logged out successfully", "/auth/logout"
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate Forgot Password", description = "Sends reset instructions to user")
    public ResponseEntity<SuccessResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Password reset instructions sent", "/auth/forgot-password"
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Resets user password using token")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Password reset successfully", "/auth/reset-password"
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Changes password for logged in user")
    public ResponseEntity<SuccessResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Password changed successfully", "/auth/change-password"
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Returns details of currently authenticated user")
    public ResponseEntity<SuccessResponse<CurrentUserResponse>> getCurrentUser() {
        CurrentUserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Current user fetched", "/auth/me", response
        ));
    }

    @GetMapping("/session")
    @Operation(summary = "Get Current Session", description = "Returns current active session details")
    public ResponseEntity<SuccessResponse<UserSessionResponse>> getCurrentSession() {
        UserSessionResponse response = authService.getCurrentSession();
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Current session fetched", "/auth/session", response
        ));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get All Active Sessions", description = "Returns all active sessions for current user")
    public ResponseEntity<SuccessResponse<List<UserSessionResponse>>> getActiveSessions() {
        List<UserSessionResponse> response = authService.getActiveSessions();
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Active sessions fetched", "/auth/sessions", response
        ));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Logout specific session", description = "Revokes a specific session")
    public ResponseEntity<SuccessResponse<Void>> logoutSession(@PathVariable String sessionId) {
        authService.logoutSession(sessionId);
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Session logged out successfully", "/auth/sessions/" + sessionId
        ));
    }

    @DeleteMapping("/logout-all-devices")
    @Operation(summary = "Logout all devices", description = "Revokes all sessions for current user")
    public ResponseEntity<SuccessResponse<Void>> logoutAllDevices() {
        authService.logoutAllDevices();
        return ResponseEntity.ok(new SuccessResponse<>(
                200, "Logged out from all devices", "/auth/logout-all-devices"
        ));
    }
}
