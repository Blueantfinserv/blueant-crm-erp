package com.blueant_crm_erp.common.base;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base Request
 *
 * Parent class for all Request DTOs.
 *
 * Used By:
 * - Auth
 * - User
 * - Role
 * - Hierarchy
 * - Lead
 * - Followup
 * - Meeting
 * - Client
 * - CRM
 * - Service Request
 * - Transaction
 * - Helpdesk
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique request id.
     */
    @Size(max = 100)
    private String requestId;

    /**
     * Request creation time.
     */
    private LocalDateTime requestTime = LocalDateTime.now();

    /**
     * Client IP Address.
     */
    @Size(max = 45)
    private String ipAddress;

    /**
     * Browser / Mobile App / Postman
     */
    @Size(max = 100)
    private String source;

    /**
     * Device Information.
     */
    @Size(max = 200)
    private String device;

    /**
     * User Agent.
     */
    @Size(max = 500)
    private String userAgent;

    /**
     * Correlation Id.
     */
    @Size(max = 100)
    private String correlationId;
}