package com.blueant_crm_erp.util.meeting;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Utility class for Meeting Verification.
 *
 * Responsibilities:
 * - Meeting verification helpers
 * - Attendance validation
 * - Completion validation
 * - Meeting proof validation
 * - Generic verification methods
 *
 * This utility DOES NOT:
 * - Access database
 * - Verify meeting from repository
 * - Approve meetings
 * - Update meeting status
 *
 * Business logic belongs to:
 * - MeetingVerificationService
 * - MeetingService
 *
 * Used By:
 * - Meeting Module
 * - Lead Module
 * - CRM Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class MeetingVerification {

    /**
     * Minimum meeting notes length.
     */
    private static final int MIN_NOTES_LENGTH = 10;

    private MeetingVerification() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if meeting exists.
     */
    public static boolean isMeetingPresent(Long meetingId) {
        return meetingId != null;
    }

    /**
     * Returns true if meeting has started.
     */
    public static boolean hasStarted(LocalDateTime startTime) {

        return MeetingUtil.hasStarted(startTime);
    }

    /**
     * Returns true if meeting has ended.
     */
    public static boolean hasEnded(LocalDateTime endTime) {

        Objects.requireNonNull(endTime);

        return endTime.isBefore(LocalDateTime.now());
    }

    /**
     * Returns true if meeting is completed.
     */
    public static boolean isCompleted(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        return hasStarted(startTime)
                && hasEnded(endTime);
    }

    /**
     * Returns true if organizer attended.
     */
    public static boolean organizerPresent(Boolean present) {
        return Boolean.TRUE.equals(present);
    }

    /**
     * Returns true if client attended.
     */
    public static boolean clientPresent(Boolean present) {
        return Boolean.TRUE.equals(present);
    }

    /**
     * Returns true if both parties attended.
     */
    public static boolean bothPresent(
            Boolean organizerPresent,
            Boolean clientPresent) {

        return Boolean.TRUE.equals(organizerPresent)
                && Boolean.TRUE.equals(clientPresent);
    }

    /**
     * Returns true if meeting proof is uploaded.
     */
    public static boolean hasMeetingProof(String proofUrl) {

        return proofUrl != null
                && !proofUrl.isBlank();
    }

    /**
     * Returns true if meeting notes are valid.
     */
    public static boolean hasValidNotes(String notes) {

        return notes != null
                && notes.trim().length() >= MIN_NOTES_LENGTH;
    }

    /**
     * Returns true if meeting can be verified.
     */
    public static boolean canVerify(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean organizerPresent,
            Boolean clientPresent) {

        return isCompleted(startTime, endTime)
                && bothPresent(organizerPresent, clientPresent);
    }

}