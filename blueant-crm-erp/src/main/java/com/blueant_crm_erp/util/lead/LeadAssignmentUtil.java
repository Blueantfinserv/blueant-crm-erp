package com.blueant_crm_erp.util.lead;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for Lead Assignment operations.
 *
 * Responsibilities:
 * - Validate assignment
 * - Compare assignees
 * - Check assignment eligibility
 * - Assignment helper methods
 *
 * This utility DOES NOT:
 * - Access database
 * - Assign leads
 * - Apply business rules
 * - Execute transfer logic
 *
 * Business rules belong to:
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
public final class LeadAssignmentUtil {

    private LeadAssignmentUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if lead is assigned.
     */
    public static boolean isAssigned(Long assigneeId) {
        return assigneeId != null;
    }

    /**
     * Returns true if lead is unassigned.
     */
    public static boolean isUnassigned(Long assigneeId) {
        return assigneeId == null;
    }

    /**
     * Returns true if assignment is changing.
     */
    public static boolean isReassignment(
            Long currentAssigneeId,
            Long newAssigneeId) {

        return currentAssigneeId != null
                && newAssigneeId != null
                && !Objects.equals(currentAssigneeId, newAssigneeId);
    }

    /**
     * Returns true if assignment remains same.
     */
    public static boolean isSameAssignee(
            Long firstAssigneeId,
            Long secondAssigneeId) {

        return Objects.equals(firstAssigneeId, secondAssigneeId);
    }

    /**
     * Returns true if user is assigning lead to himself.
     */
    public static boolean isSelfAssignment(
            Long loggedInUserId,
            Long assigneeId) {

        return Objects.equals(loggedInUserId, assigneeId);
    }

    /**
     * Checks whether assignee exists.
     */
    public static boolean hasAssignee(Long assigneeId) {

        return assigneeId != null;
    }

    /**
     * Checks whether leader exists.
     */
    public static boolean hasLeader(Long leaderId) {

        return leaderId != null;
    }

    /**
     * Returns total assigned leads.
     */
    public static int assignedLeadCount(Collection<?> assignedLeads) {

        return assignedLeads == null
                ? 0
                : assignedLeads.size();
    }

    /**
     * Returns true if assignment limit is reached.
     */
    public static boolean hasReachedLimit(
            int assignedCount,
            int maxLimit) {

        return assignedCount >= maxLimit;
    }

    /**
     * Returns remaining assignment capacity.
     */
    public static int remainingCapacity(
            int assignedCount,
            int maxLimit) {

        return Math.max(maxLimit - assignedCount, 0);
    }

    /**
     * Returns true if assignment request is valid.
     */
    public static boolean isValidAssignment(
            Long leadId,
            Long assigneeId) {

        return leadId != null
                && assigneeId != null;
    }

    /**
     * Returns true if assignment can proceed.
     */
    public static boolean canAssign(
            Long leadId,
            Long assigneeId,
            boolean assigneeActive) {

        return leadId != null
                && assigneeId != null
                && assigneeActive;
    }

}