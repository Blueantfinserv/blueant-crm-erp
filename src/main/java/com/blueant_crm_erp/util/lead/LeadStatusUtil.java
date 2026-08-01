package com.blueant_crm_erp.util.lead;

import com.blueant_crm_erp.common.enums.LeadStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for Lead Status operations.
 *
 * Responsibilities:
 * - Status validation
 * - Active status checks
 * - Terminal status checks
 * - Closed status checks
 * - Generic status helper methods
 *
 * This utility DOES NOT:
 * - Access database
 * - Execute business workflow
 * - Validate workflow transitions
 * - Update lead status
 *
 * Workflow validation belongs to:
 * - LeadTransitionValidator
 * - LeadWorkflowService
 *
 * Used By:
 * - Lead Module
 * - Dashboard Module
 * - Reports Module
 * - Analytics Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class LeadStatusUtil {

    /**
     * Lead statuses that are considered active.
     */
    private static final Set<LeadStatus> ACTIVE_STATUSES =
            Collections.unmodifiableSet(EnumSet.of(
                    LeadStatus.NEW,
                    LeadStatus.ASSIGNED,
                    LeadStatus.WORK_IN_PROGRESS,
                    LeadStatus.FOLLOW_UP_PENDING,
                    LeadStatus.FOLLOW_UP_COMPLETED,
                    LeadStatus.MEETING_SCHEDULED,
                    LeadStatus.MEETING_COMPLETED
            ));

    /**
     * Lead statuses where lifecycle ends.
     */
    private static final Set<LeadStatus> TERMINAL_STATUSES =
            Collections.unmodifiableSet(EnumSet.of(
                    LeadStatus.CONVERTED,
                    LeadStatus.ALREADY_CLIENT,
                    LeadStatus.NOT_INTERESTED,
                    LeadStatus.REMOVED
            ));

    private LeadStatusUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if status is active.
     */
    public static boolean isActive(LeadStatus status) {

        requireStatus(status);

        return ACTIVE_STATUSES.contains(status);
    }

    /**
     * Returns true if status is terminal.
     */
    public static boolean isTerminal(LeadStatus status) {

        requireStatus(status);

        return TERMINAL_STATUSES.contains(status);
    }

    /**
     * Returns true if lead is closed.
     */
    public static boolean isClosed(LeadStatus status) {

        requireStatus(status);

        return TERMINAL_STATUSES.contains(status);
    }

    /**
     * Returns true if lead is converted.
     */
    public static boolean isConverted(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.CONVERTED.equals(status);
    }

    /**
     * Returns true if lead already belongs to BlueAnt.
     */
    public static boolean isAlreadyClient(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.ALREADY_CLIENT.equals(status);
    }

    /**
     * Returns true if client is not interested.
     */
    public static boolean isNotInterested(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.NOT_INTERESTED.equals(status);
    }

    /**
     * Returns true if lead is removed.
     */
    public static boolean isRemoved(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.REMOVED.equals(status);
    }

    /**
     * Returns true if follow-up is pending.
     */
    public static boolean isFollowUpPending(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.FOLLOW_UP_PENDING.equals(status);
    }

    /**
     * Returns true if follow-up is completed.
     */
    public static boolean isFollowUpCompleted(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.FOLLOW_UP_COMPLETED.equals(status);
    }

    /**
     * Returns true if meeting is scheduled.
     */
    public static boolean isMeetingScheduled(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.MEETING_SCHEDULED.equals(status);
    }

    /**
     * Returns true if meeting is completed.
     */
    public static boolean isMeetingCompleted(LeadStatus status) {

        requireStatus(status);

        return LeadStatus.MEETING_COMPLETED.equals(status);
    }

    /**
     * Returns true if status is valid.
     */
    public static boolean isValid(LeadStatus status) {
        return status != null;
    }

    /**
     * Returns immutable active statuses.
     */
    public static Set<LeadStatus> getActiveStatuses() {
        return ACTIVE_STATUSES;
    }

    /**
     * Returns immutable terminal statuses.
     */
    public static Set<LeadStatus> getTerminalStatuses() {
        return TERMINAL_STATUSES;
    }

    /**
     * Validates lead status.
     */
    private static void requireStatus(LeadStatus status) {

        Objects.requireNonNull(
                status,
                "Lead status must not be null."
        );
    }

}