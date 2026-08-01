package com.blueant_crm_erp.util.lead;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Utility class for Lead Transfer operations.
 *
 * Responsibilities:
 * - Validate transfer request
 * - Check reassignment
 * - Validate transfer reason
 * - Validate transfer dates
 * - Generic transfer helper methods
 *
 * This utility DOES NOT:
 * - Access database
 * - Transfer leads
 * - Check duplicate lead rules
 * - Execute approval workflow
 *
 * Business logic belongs to:
 * - LeadTransferService
 * - LeadAssignmentService
 *
 * Used By:
 * - Lead Module
 * - Lead Assignment Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class LeadTransferUtil {

    /**
     * Minimum transfer reason length.
     */
    private static final int MIN_REASON_LENGTH = 10;

    /**
     * Maximum transfer reason length.
     */
    private static final int MAX_REASON_LENGTH = 500;

    private LeadTransferUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if lead is transferred
     * to another employee.
     */
    public static boolean isTransfer(
            Long currentAssigneeId,
            Long newAssigneeId) {

        return currentAssigneeId != null
                && newAssigneeId != null
                && !Objects.equals(currentAssigneeId, newAssigneeId);
    }

    /**
     * Returns true if both assignees are same.
     */
    public static boolean isSameAssignee(
            Long currentAssigneeId,
            Long newAssigneeId) {

        return Objects.equals(
                currentAssigneeId,
                newAssigneeId
        );
    }

    /**
     * Returns true if user is transferring
     * lead to himself.
     */
    public static boolean isSelfTransfer(
            Long loggedInUserId,
            Long targetUserId) {

        return Objects.equals(
                loggedInUserId,
                targetUserId
        );
    }

    /**
     * Validates transfer reason.
     */
    public static boolean isValidReason(
            String reason) {

        if (reason == null) {
            return false;
        }

        String value = reason.trim();

        return value.length() >= MIN_REASON_LENGTH
                && value.length() <= MAX_REASON_LENGTH;
    }

    /**
     * Returns true if transfer request
     * contains mandatory values.
     */
    public static boolean isValidTransferRequest(
            Long leadId,
            Long fromUserId,
            Long toUserId) {

        return leadId != null
                && fromUserId != null
                && toUserId != null;
    }

    /**
     * Returns true if transfer date is valid.
     */
    public static boolean isValidTransferDate(
            LocalDateTime transferDate) {

        return transferDate != null
                && !transferDate.isAfter(LocalDateTime.now());
    }

    /**
     * Returns true if transfer can proceed.
     *
     * NOTE:
     * Does NOT validate business rules.
     */
    public static boolean canTransfer(
            Long leadId,
            Long fromUserId,
            Long toUserId,
            boolean targetUserActive) {

        return isValidTransferRequest(
                leadId,
                fromUserId,
                toUserId
        )
                && targetUserActive
                && !Objects.equals(fromUserId, toUserId);
    }

}