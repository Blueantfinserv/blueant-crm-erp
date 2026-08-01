package com.blueant_crm_erp.exception.base;

/**
 * Centralized Exception Messages
 *
 * Contains all reusable exception messages
 * for the BlueAnt CRM ERP Platform.
 *
 * Every custom exception should use these
 * constants instead of hardcoded messages.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ExceptionMessage {

    private ExceptionMessage() {
        throw new IllegalStateException("Utility class");
    }

    /* ==========================================================
                       COMMON
       ========================================================== */

    public static final String INTERNAL_SERVER_ERROR =
            "An unexpected error occurred. Please contact the administrator.";

    public static final String BAD_REQUEST =
            "Invalid request.";

    public static final String VALIDATION_FAILED =
            "Validation failed.";

    public static final String RESOURCE_NOT_FOUND =
            "Requested resource not found.";

    public static final String RESOURCE_ALREADY_EXISTS =
            "Resource already exists.";

    public static final String DATABASE_ERROR =
            "Database operation failed.";

    public static final String ACCESS_DENIED =
            "You do not have permission to perform this action.";

    public static final String OPERATION_NOT_ALLOWED =
            "Operation is not allowed.";

    /* ==========================================================
                       AUTH MODULE
       ========================================================== */

    public static final String UNAUTHORIZED =
            "Authentication is required to access this resource.";

    public static final String INVALID_CREDENTIALS =
            "Invalid username or password.";

    public static final String INVALID_TOKEN =
            "Invalid authentication token.";

    public static final String TOKEN_EXPIRED =
            "Access token has expired.";

    public static final String REFRESH_TOKEN_EXPIRED =
            "Refresh token has expired. Please login again.";

    public static final String ACCOUNT_LOCKED =
            "Your account has been locked.";

    public static final String ACCOUNT_DISABLED =
            "Your account has been disabled.";

    public static final String ACCOUNT_EXPIRED =
            "Your account has expired.";

    public static final String SESSION_EXPIRED =
            "Your session has expired.";

    public static final String PASSWORD_MISMATCH =
            "Password does not match.";

    public static final String OLD_PASSWORD_INCORRECT =
            "Old password is incorrect.";

    /* ==========================================================
                       USER MODULE
       ========================================================== */

    public static final String USER_LOCKED =
            "The user account has been locked. Please contact the system administrator.";

    public static final String INVALID_USER_STATUS =
            "The user account is not in a valid status to perform this operation.";

    public static final String USER_NOT_FOUND =
            "User not found.";

    public static final String USER_ALREADY_EXISTS =
            "User already exists.";

    public static final String USER_INACTIVE =
            "User is inactive.";

    /* ==========================================================
                       ROLE MODULE
       ========================================================== */
    public static final String ROLE_ALREADY_ASSIGNED =
            "The role is already assigned to the user.";

    public static final String ROLE_NOT_FOUND =
            "Role not found.";

    public static final String ROLE_ALREADY_EXISTS =
            "Role already exists.";

    public static final String ROLE_IN_USE =
            "Role cannot be deleted because it is assigned to users.";

    /* ==========================================================
                       HIERARCHY MODULE
       ========================================================== */
    public static final String HIERARCHY_ERROR =
            "Invalid organizational hierarchy.";

    public static final String INVALID_HIERARCHY =
            "Invalid reporting hierarchy.";

    public static final String MANAGER_NOT_FOUND =
            "Reporting manager not found.";

    public static final String INVALID_REPORTING_STRUCTURE =
            "Invalid reporting structure.";

    /* ==========================================================
                       MAPPING MODULE
       ========================================================== */

    public static final String MAPPING_NOT_FOUND =
            "Mapping not found.";

    public static final String DUPLICATE_MAPPING =
            "Mapping already exists.";

    /* ==========================================================
                       LEAD MODULE
       ========================================================== */
    public static final String LEAD_TRANSFER_ERROR =
            "Lead transfer could not be completed due to business rule violation.";
    public static final String LEAD_ACCESS_DENIED =
            "You are not authorized to access or modify this lead.";
    public static final String INVALID_LEAD_STATUS =
            "The requested lead status or status transition is not allowed.";

    public static final String LEAD_NOT_FOUND =
            "Lead not found.";

    public static final String DUPLICATE_LEAD =
            "Duplicate lead detected.";

    public static final String LEAD_ALREADY_CONVERTED =
            "Lead has already been converted.";

    public static final String LEAD_TRANSFER_NOT_ALLOWED =
            "Lead transfer is not allowed.";

    public static final String LEAD_NOT_ASSIGNED =
            "Lead is not assigned.";

    /* ==========================================================
                       FOLLOW-UP MODULE
       ========================================================== */

    public static final String FOLLOWUP_NOT_FOUND =
            "Follow-up not found.";

    public static final String FOLLOWUP_ALREADY_COMPLETED =
            "Follow-up has already been completed.";

    /* ==========================================================
                       MEETING MODULE
       ========================================================== */
    public static final String MEETING_VALIDATION_FAILED =
            "Meeting validation failed.";

    public static final String MEETING_NOT_FOUND =
            "Meeting not found.";

    public static final String MEETING_ALREADY_VERIFIED =
            "Meeting has already been verified.";

    /* ==========================================================
                       CLIENT MODULE
       ========================================================== */
    public static final String INVALID_CLIENT_STATUS =
            "Client status does not allow the requested operation.";

    public static final String CLIENT_NOT_FOUND =
            "Client not found.";

    public static final String CLIENT_ALREADY_EXISTS =
            "Client already exists.";

    /* ==========================================================
                    SERVICE REQUEST MODULE
       ========================================================== */
    public static final String INVALID_REQUEST =
            "The request is invalid. Please verify the submitted data.";
    
    public static final String SERVICE_ALREADY_COMPLETED =
            "The requested service has already been completed.";
    public static final String DUPLICATE_RESOURCE =
            "A resource with the same details already exists.";
    public static final String SERVICE_REQUEST_NOT_FOUND =
            "Service request not found.";

    /* ==========================================================
                     TRANSACTION MODULE
       ========================================================== */

    public static final String TRANSACTION_NOT_FOUND =
            "Transaction not found.";

    public static final String PAYMENT_NOT_RECEIVED =
            "First payment has not been received.";

    /* ==========================================================
                     INCENTIVE MODULE
       ========================================================== */

    public static final String INCENTIVE_NOT_FOUND =
            "Incentive record not found.";

    /* ==========================================================
                     HELPDESK MODULE
       ========================================================== */

    public static final String TICKET_NOT_FOUND =
            "Helpdesk ticket not found.";

    /* ==========================================================
                     FILE MODULE
       ========================================================== */
    public static final String FILE_STORAGE_ERROR =
            "Failed to store the file. Please try again later.";
    public static final String FILE_NOT_FOUND =
            "File not found.";

    public static final String FILE_UPLOAD_FAILED =
            "File upload failed.";

    public static final String INVALID_FILE_TYPE =
            "Invalid file type.";

    /* ==========================================================
                     NOTIFICATION MODULE
       ========================================================== */

    public static final String EMAIL_SEND_FAILED =
            "Failed to send email.";

    public static final String SMS_SEND_FAILED =
            "Failed to send SMS.";

    public static final String WHATSAPP_SEND_FAILED =
            "Failed to send WhatsApp message.";

}