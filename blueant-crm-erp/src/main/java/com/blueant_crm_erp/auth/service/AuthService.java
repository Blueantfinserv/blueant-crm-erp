package com.blueant_crm_erp.auth.service;

import com.blueant_crm_erp.auth.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.auth.dto.request.ForgotPasswordRequest;
import com.blueant_crm_erp.auth.dto.request.LoginRequest;
import com.blueant_crm_erp.auth.dto.request.LogoutRequest;
import com.blueant_crm_erp.auth.dto.request.RefreshTokenRequest;
import com.blueant_crm_erp.auth.dto.request.ResetPasswordRequest;
import com.blueant_crm_erp.auth.dto.request.VerifyLoginOtpRequest;
import com.blueant_crm_erp.auth.dto.response.CurrentUserResponse;
import com.blueant_crm_erp.auth.dto.response.LoginResponse;
import com.blueant_crm_erp.auth.dto.response.RefreshTokenResponse;
import com.blueant_crm_erp.auth.dto.response.UserSessionResponse;

import java.util.List;

/**
 * =============================================================================
 * Authentication Service
 * =============================================================================
 *
 * Business contract for Authentication & Authorization.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • User Authentication
 * • Login OTP Verification
 * • JWT Token Management
 * • Password Management
 * • Session Management
 * • Current User Information
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface AuthService {

    // =========================================================================
    // Authentication
    // =========================================================================

    /**
     * Authenticates a user using login credentials.
     *
     * @param request login request
     * @return login response
     */
    LoginResponse login(LoginRequest request);

    /**
     * Verifies Login OTP and completes authentication.
     *
     * @param request verify login OTP request
     * @return login response
     */
    LoginResponse verifyLoginOtp(
            VerifyLoginOtpRequest request
    );

    /**
     * Refreshes JWT Access Token.
     *
     * @param request refresh token request
     * @return refresh token response
     */
    RefreshTokenResponse refreshToken(
            RefreshTokenRequest request
    );

    /**
     * Logs out current session.
     *
     * @param request logout request
     */
    void logout(
            LogoutRequest request
    );

    // =========================================================================
    // Password Management
    // =========================================================================

    /**
     * Initiates forgot password process.
     *
     * @param request forgot password request
     */
    void forgotPassword(
            ForgotPasswordRequest request
    );

    /**
     * Resets user password.
     *
     * @param request reset password request
     */
    void resetPassword(
            ResetPasswordRequest request
    );

    /**
     * Changes current user's password.
     *
     * @param request change password request
     */
    void changePassword(
            ChangePasswordRequest request
    );

    // =========================================================================
    // Current User
    // =========================================================================

    /**
     * Returns currently authenticated user.
     *
     * @return current user response
     */
    CurrentUserResponse getCurrentUser();

    // =========================================================================
    // Session Management
    // =========================================================================

    /**
     * Returns current session.
     *
     * @return current session
     */
    UserSessionResponse getCurrentSession();

    /**
     * Returns all active sessions.
     *
     * @return active sessions
     */
    List<UserSessionResponse> getActiveSessions();

    /**
     * Logs out a specific session.
     *
     * @param sessionId session identifier
     */
    void logoutSession(
            String sessionId
    );

    /**
     * Logs out from all devices.
     */
    void logoutAllDevices();

}