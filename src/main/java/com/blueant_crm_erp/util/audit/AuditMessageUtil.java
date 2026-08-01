package com.blueant_crm_erp.util.audit;

import java.text.MessageFormat;

/**
 * Utility class for generating standardized audit log messages.
 *
 * This class centralizes all audit messages used across the
 * BlueAnt CRM ERP Platform.
 *
 * Modules:
 * - Authentication
 * - User
 * - Role
 * - Hierarchy
 * - Lead
 * - Client
 * - Meeting
 * - Service Request
 * - Transaction
 * - Incentive
 *
 * Example:
 *
 * User 'EMP1001' created Lead 'LEAD-20260001'
 * User 'ADMIN001' updated Role 'CRM_MANAGER'
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class AuditMessageUtil {

    private AuditMessageUtil() {
        throw new IllegalStateException("Utility class");
    }

    /* ==========================================================
     * COMMON
     * ==========================================================
     */

    public static String created(String entity, Object identifier) {
        return MessageFormat.format(
                "{0} ''{1}'' has been created.",
                entity,
                identifier
        );
    }

    public static String updated(String entity, Object identifier) {
        return MessageFormat.format(
                "{0} ''{1}'' has been updated.",
                entity,
                identifier
        );
    }

    public static String deleted(String entity, Object identifier) {
        return MessageFormat.format(
                "{0} ''{1}'' has been deleted.",
                entity,
                identifier
        );
    }

    public static String viewed(String entity, Object identifier) {
        return MessageFormat.format(
                "{0} ''{1}'' has been viewed.",
                entity,
                identifier
        );
    }

    /* ==========================================================
     * USER
     * ==========================================================
     */

    public static String userLoggedIn(String employeeCode) {
        return MessageFormat.format(
                "User ''{0}'' logged into the system.",
                employeeCode
        );
    }

    public static String userLoggedOut(String employeeCode) {
        return MessageFormat.format(
                "User ''{0}'' logged out from the system.",
                employeeCode
        );
    }

    public static String passwordChanged(String employeeCode) {
        return MessageFormat.format(
                "Password changed for user ''{0}''.",
                employeeCode
        );
    }

    public static String accountLocked(String employeeCode) {
        return MessageFormat.format(
                "User account ''{0}'' has been locked.",
                employeeCode
        );
    }

    public static String accountUnlocked(String employeeCode) {
        return MessageFormat.format(
                "User account ''{0}'' has been unlocked.",
                employeeCode
        );
    }

    /* ==========================================================
     * ROLE
     * ==========================================================
     */

    public static String roleAssigned(String roleName, String employeeCode) {
        return MessageFormat.format(
                "Role ''{0}'' assigned to user ''{1}''.",
                roleName,
                employeeCode
        );
    }

    public static String roleRemoved(String roleName, String employeeCode) {
        return MessageFormat.format(
                "Role ''{0}'' removed from user ''{1}''.",
                roleName,
                employeeCode
        );
    }

    /* ==========================================================
     * LEAD
     * ==========================================================
     */

    public static String leadAssigned(String leadCode, String employeeCode) {
        return MessageFormat.format(
                "Lead ''{0}'' assigned to ''{1}''.",
                leadCode,
                employeeCode
        );
    }

    public static String leadTransferred(
            String leadCode,
            String fromEmployee,
            String toEmployee) {

        return MessageFormat.format(
                "Lead ''{0}'' transferred from ''{1}'' to ''{2}''.",
                leadCode,
                fromEmployee,
                toEmployee
        );
    }

    public static String leadConverted(String leadCode) {
        return MessageFormat.format(
                "Lead ''{0}'' converted into client.",
                leadCode
        );
    }

    /* ==========================================================
     * CLIENT
     * ==========================================================
     */

    public static String clientCreated(String clientCode) {
        return MessageFormat.format(
                "Client ''{0}'' created.",
                clientCode
        );
    }

    /* ==========================================================
     * MEETING
     * ==========================================================
     */

    public static String meetingScheduled(String meetingCode) {
        return MessageFormat.format(
                "Meeting ''{0}'' scheduled.",
                meetingCode
        );
    }

    public static String meetingVerified(String meetingCode) {
        return MessageFormat.format(
                "Meeting ''{0}'' verified by Process Coordinator.",
                meetingCode
        );
    }

    /* ==========================================================
     * SERVICE REQUEST
     * ==========================================================
     */

    public static String serviceRequestCreated(String requestNumber) {
        return MessageFormat.format(
                "Service Request ''{0}'' created.",
                requestNumber
        );
    }

    public static String serviceCompleted(String requestNumber) {
        return MessageFormat.format(
                "Service Request ''{0}'' completed.",
                requestNumber
        );
    }

    /* ==========================================================
     * TRANSACTION
     * ==========================================================
     */

    public static String paymentReceived(String transactionNumber) {
        return MessageFormat.format(
                "Payment received for transaction ''{0}''.",
                transactionNumber
        );
    }

    public static String incentiveGenerated(String incentiveNumber) {
        return MessageFormat.format(
                "Incentive ''{0}'' generated.",
                incentiveNumber
        );
    }

}