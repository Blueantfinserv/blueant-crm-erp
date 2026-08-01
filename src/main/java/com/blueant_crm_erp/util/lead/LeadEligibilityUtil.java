package com.blueant_crm_erp.util.lead;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for Lead Eligibility.
 *
 * Responsibilities:
 * - Generic eligibility checks
 * - Lead lifecycle helper methods
 * - Validation helpers
 *
 * This utility DOES NOT:
 * - Access database
 * - Perform business workflow
 * - Apply duplicate lead rules
 * - Assign leads
 *
 * Business logic belongs to:
 * - LeadService
 * - LeadAssignmentService
 * - FollowUpService
 *
 * Used By:
 * - Lead Module
 * - Follow-up Module
 * - Meeting Module
 * - Client Conversion Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class LeadEligibilityUtil {

    private LeadEligibilityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if lead exists.
     */
    public static boolean exists(Long leadId) {
        return leadId != null;
    }

    /**
     * Returns true if lead is active.
     */
    public static boolean isActive(Boolean active) {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Returns true if lead is eligible for follow-up.
     */
    public static boolean eligibleForFollowUp(
            Boolean active,
            LocalDate nextFollowUpDate) {

        return isActive(active)
                && nextFollowUpDate != null
                && !nextFollowUpDate.isAfter(LocalDate.now());
    }

    /**
     * Returns true if lead is eligible for meeting.
     */
    public static boolean eligibleForMeeting(
            Boolean active,
            Boolean followUpCompleted) {

        return isActive(active)
                && Boolean.TRUE.equals(followUpCompleted);
    }

    /**
     * Returns true if lead is eligible for conversion.
     */
    public static boolean eligibleForConversion(
            Boolean active,
            Boolean meetingCompleted) {

        return isActive(active)
                && Boolean.TRUE.equals(meetingCompleted);
    }

    /**
     * Returns true if lead is eligible for Service Request.
     */
    public static boolean eligibleForServiceRequest(
            Boolean converted) {

        return Boolean.TRUE.equals(converted);
    }

    /**
     * Returns true if lead can be reassigned.
     *
     * Business rules should be checked separately.
     */
    public static boolean eligibleForReassignment(
            Boolean active,
            Long currentAssigneeId) {

        return isActive(active)
                && currentAssigneeId != null;
    }

    /**
     * Returns true if lead can be closed.
     */
    public static boolean eligibleForClosure(
            Boolean active,
            Boolean converted) {

        return isActive(active)
                || Boolean.TRUE.equals(converted);
    }

    /**
     * Returns true if closed lead can be reopened.
     */
    public static boolean eligibleForReopen(
            Boolean closed,
            Boolean archived) {

        return Boolean.TRUE.equals(closed)
                && !Boolean.TRUE.equals(archived);
    }

    /**
     * Returns true if next follow-up date is due.
     */
    public static boolean isFollowUpDue(
            LocalDate nextFollowUpDate) {

        return nextFollowUpDate != null
                && !nextFollowUpDate.isAfter(LocalDate.now());
    }

    /**
     * Returns true if lead is assigned.
     */
    public static boolean isAssigned(Long assigneeId) {
        return assigneeId != null;
    }

    /**
     * Returns true if lead has valid owner.
     */
    public static boolean hasOwner(Long ownerId) {
        return ownerId != null;
    }

    /**
     * Generic null-safe validation.
     */
    public static boolean isValid(Long leadId) {

        return Objects.nonNull(leadId);
    }

}