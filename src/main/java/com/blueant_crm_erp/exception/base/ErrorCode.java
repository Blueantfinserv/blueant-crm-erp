package com.blueant_crm_erp.exception.base;

/**
 * Error Codes
 *
 * Centralized error codes used across the BlueAnt CRM ERP Platform.
 *
 * Every custom exception should use one of these codes.
 *
 * Example:
 * USER_NOT_FOUND
 * LEAD_NOT_FOUND
 * INVALID_TOKEN
 * DUPLICATE_LEAD
 *
 * @author BlueAnt
 * @version 1.0
 */
public enum ErrorCode {

    /*
     * =====================================================
     * COMMON
     * =====================================================
     */
    INTERNAL_SERVER_ERROR,
    BAD_REQUEST,
    RESOURCE_NOT_FOUND,
    RESOURCE_ALREADY_EXISTS,
    VALIDATION_FAILED,
    DATABASE_ERROR,
    FILE_UPLOAD_FAILED,
    FILE_NOT_FOUND,

    /*
     * =====================================================
     * AUTH MODULE
     * =====================================================
     */
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    TOKEN_EXPIRED,
    REFRESH_TOKEN_EXPIRED,
    ACCESS_DENIED,
    UNAUTHORIZED,
    ACCOUNT_LOCKED,
    ACCOUNT_DISABLED,

    /*
     * =====================================================
     * USER MODULE
     * =====================================================
     */
    USER_NOT_FOUND,
    USER_ALREADY_EXISTS,
    USER_INACTIVE,
    USER_LOCKED,
    INVALID_USER_STATUS,

    /*
     * =====================================================
     * ROLE MODULE
     * =====================================================
     */
    ROLE_NOT_FOUND,
    ROLE_ALREADY_EXISTS,
    ROLE_ALREADY_ASSIGNED,

    /*
     * =====================================================
     * HIERARCHY MODULE
     * =====================================================
     */
    HIERARCHY_ERROR,
    DUPLICATE_RESOURCE,
    MANAGER_NOT_FOUND,
    INVALID_HIERARCHY,
    CIRCULAR_HIERARCHY,

    /*
     * =====================================================
     * LEAD MODULE
     * =====================================================
     */
    LEAD_TRANSFER_ERROR,
    LEAD_NOT_FOUND,
    DUPLICATE_LEAD,
    LEAD_ALREADY_CONVERTED,
    LEAD_ACCESS_DENIED,
    LEAD_TRANSFER_NOT_ALLOWED,
    INVALID_LEAD_STATUS,
    LEAD_TERMINAL_STATE,

    /*
     * =====================================================
     * FOLLOW-UP MODULE
     * =====================================================
     */
    FOLLOWUP_NOT_FOUND,
    FOLLOWUP_ALREADY_COMPLETED,
    INVALID_FOLLOWUP_DATE,

    /*
     * =====================================================
     * MEETING MODULE
     * =====================================================
     */
    MEETING_VALIDATION_FAILED,
    MEETING_NOT_FOUND,
    MEETING_ALREADY_VERIFIED,
    INVALID_MEETING_STATUS,
    INVALID_MEETING_DATE,

    /*
     * =====================================================
     * CLIENT MODULE
     * =====================================================
     */
    INVALID_CLIENT_STATUS,
    CLIENT_NOT_FOUND,
    CLIENT_ALREADY_EXISTS,
    CLIENT_ALREADY_ONBOARDED,

    /*
     * =====================================================
     * ONBOARDING MODULE
     * =====================================================
     */
    DOCUMENT_NOT_FOUND,
    DOCUMENT_UPLOAD_FAILED,
    INVALID_DOCUMENT,

    /*
     * =====================================================
     * SERVICE REQUEST
     * =====================================================
     */
    INVALID_REQUEST,
    SERVICE_REQUEST_NOT_FOUND,
    SERVICE_ALREADY_COMPLETED,

    /*
     * =====================================================
     * CRM MODULE
     * =====================================================
     */
    CRM_RECORD_NOT_FOUND,

    /*
     * =====================================================
     * TRANSACTION MODULE
     * =====================================================
     */
    TRANSACTION_NOT_FOUND,
    PAYMENT_NOT_RECEIVED,
    INVALID_TRANSACTION,
    DUPLICATE_TRANSACTION,

    /*
     * =====================================================
     * FMS MODULE
     * =====================================================
     */ 
    FILE_STORAGE_ERROR, //file error
    PORTFOLIO_NOT_FOUND,

    /*
     * =====================================================
     * INCENTIVE MODULE
     * =====================================================
     */
    INCENTIVE_NOT_FOUND,
    INCENTIVE_ALREADY_GENERATED,

    /*
     * =====================================================
     * HELPDESK MODULE
     * =====================================================
     */
    TICKET_NOT_FOUND,
    TICKET_ALREADY_CLOSED,

    /*
     * =====================================================
     * NOTIFICATION MODULE
     * =====================================================
     */
    NOTIFICATION_NOT_FOUND,
    EMAIL_SEND_FAILED,
    SMS_SEND_FAILED,
    WHATSAPP_SEND_FAILED,

    /*
     * =====================================================
     * REPORT MODULE
     * =====================================================
     */
    REPORT_NOT_FOUND,
    REPORT_GENERATION_FAILED
}