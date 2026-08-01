package com.blueant_crm_erp.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =============================================================================
 * Login Request
 * =============================================================================
 *
 * Request DTO used for user authentication.
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • User must provide Employee Code.
 * • Password is mandatory.
 * • Device information is collected for
 *   security monitoring and login history.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Employee Code.
     */
    @NotBlank(message = "Employee code is required.")
    @Size(
            min = 3,
            max = 30,
            message = "Employee code must be between 3 and 30 characters."
    )
    private String employeeCode;

    /**
     * User Password.
     */
    @NotBlank(message = "Password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters."
    )
    private String password;

    /**
     * Device Identifier.
     */
    @Size(max = 255,
            message = "Device ID must not exceed 255 characters.")
    private String deviceId;

    /**
     * Device Name.
     */
    @Size(max = 150,
            message = "Device name must not exceed 150 characters.")
    private String deviceName;

    /**
     * Device Type.
     *
     * Example:
     * MOBILE
     * DESKTOP
     * TABLET
     */
    @Size(max = 50,
            message = "Device type must not exceed 50 characters.")
    private String deviceType;

    /**
     * Browser Name.
     */
    @Size(max = 100,
            message = "Browser name must not exceed 100 characters.")
    private String browser;

    /**
     * Operating System.
     */
    @Size(max = 100,
            message = "Operating system must not exceed 100 characters.")
    private String operatingSystem;

    /**
     * Remember Me Flag.
     */
    @Builder.Default
    private Boolean rememberMe = Boolean.FALSE;

}