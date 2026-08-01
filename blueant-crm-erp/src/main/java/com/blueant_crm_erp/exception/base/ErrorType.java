package com.blueant_crm_erp.exception.base;

/**
 * Error Type
 *
 * Represents the category of an exception.
 *
 * Used By:
 * - BaseException
 * - GlobalExceptionHandler
 * - Logging
 * - Audit
 * - Monitoring
 *
 * Example:
 * USER_NOT_FOUND
 *      ↓
 * BUSINESS
 *
 * INVALID_TOKEN
 *      ↓
 * AUTHENTICATION
 *
 * DATABASE_ERROR
 *      ↓
 * DATABASE
 *
 * @author BlueAnt
 * @version 1.0
 */
public enum ErrorType {

    /**
     * Validation errors
     * Example:
     * Invalid Email
     * Mobile Number Missing
     * PAN Format Invalid
     */
    VALIDATION,

    /**
     * User authentication failed.
     * Example:
     * Wrong Password
     * Invalid JWT
     * Expired Token
     */
    AUTHENTICATION,

    /**
     * User has no permission.
     * Example:
     * Sales Person accessing Admin APIs.
     */
    AUTHORIZATION,

    /**
     * Business Rule Violation.
     * Example:
     * Duplicate Lead
     * Lead Already Converted
     * Meeting Already Verified
     */
    BUSINESS,

    /**
     * Requested resource not found.
     * Example:
     * User Not Found
     * Lead Not Found
     * Client Not Found
     */
    RESOURCE,

    /**
     * Database related errors.
     * Example:
     * Constraint Violation
     * Connection Failure
     * SQL Exception
     */
    DATABASE,

    /**
     * File related errors.
     * Example:
     * Upload Failed
     * File Not Found
     * Invalid Document
     */
    FILE,

    /**
     * External services.
     * Example:
     * Email API Failed
     * WhatsApp API Failed
     * SMS Gateway Failed
     */
    EXTERNAL_SERVICE,

    /**
     * Cache related errors.
     * Example:
     * Redis Down
     * Cache Miss
     */
    CACHE,

    /**
     * Internal server errors.
     * Example:
     * NullPointerException
     * IllegalStateException
     */
    SYSTEM
}