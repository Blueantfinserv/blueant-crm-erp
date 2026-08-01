package com.blueant_crm_erp.util.meeting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

/**
 * Generic Meeting Utility.
 *
 * Responsibilities:
 * - Meeting state helper methods
 * - Time helper methods
 * - Null-safe meeting validations
 * - Meeting comparison helpers
 *
 * This utility DOES NOT:
 * - Schedule meetings
 * - Access database
 * - Update meetings
 * - Send reminders
 * - Generate meeting links
 *
 * Business logic belongs to:
 * - MeetingService
 * - MeetingSchedulerService
 *
 * Used By:
 * - Meeting Module
 * - Dashboard Module
 * - CRM Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class MeetingUtil {

    private MeetingUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if meeting exists.
     */
    public static boolean exists(Long meetingId) {
        return meetingId != null;
    }

    /**
     * Returns true if meeting has started.
     */
    public static boolean hasStarted(LocalDateTime startTime) {

        Objects.requireNonNull(startTime);

        return !startTime.isAfter(LocalDateTime.now());
    }

    /**
     * Returns true if meeting has ended.
     */
    public static boolean hasEnded(LocalDateTime endTime) {

        Objects.requireNonNull(endTime);

        return endTime.isBefore(LocalDateTime.now());
    }

    /**
     * Returns true if meeting is currently in progress.
     */
    public static boolean isInProgress(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        Objects.requireNonNull(startTime);
        Objects.requireNonNull(endTime);

        LocalDateTime now = LocalDateTime.now();

        return !now.isBefore(startTime)
                && !now.isAfter(endTime);
    }

    /**
     * Returns true if meeting is upcoming.
     */
    public static boolean isUpcoming(LocalDateTime startTime) {

        Objects.requireNonNull(startTime);

        return startTime.isAfter(LocalDateTime.now());
    }

    /**
     * Returns meeting duration.
     */
    public static Duration getDuration(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        Objects.requireNonNull(startTime);
        Objects.requireNonNull(endTime);

        return Duration.between(startTime, endTime);
    }

    /**
     * Returns meeting duration in minutes.
     */
    public static long getDurationMinutes(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        return getDuration(startTime, endTime).toMinutes();
    }

    /**
     * Returns true if participant list is available.
     */
    public static boolean hasParticipants(Collection<?> participants) {

        return participants != null
                && !participants.isEmpty();
    }

    /**
     * Returns participant count.
     */
    public static int participantCount(Collection<?> participants) {

        return participants == null
                ? 0
                : participants.size();
    }

    /**
     * Returns true if meeting owner exists.
     */
    public static boolean hasOrganizer(Long organizerId) {

        return organizerId != null;
    }

    /**
     * Returns true if meeting location exists.
     */
    public static boolean hasLocation(String location) {

        return location != null
                && !location.isBlank();
    }

    /**
     * Returns true if online meeting link exists.
     */
    public static boolean hasMeetingLink(String meetingLink) {

        return meetingLink != null
                && !meetingLink.isBlank();
    }

    /**
     * Returns true if notes are available.
     */
    public static boolean hasNotes(String notes) {

        return notes != null
                && !notes.isBlank();
    }

    /**
     * Returns true if follow-up is required.
     */
    public static boolean requiresFollowUp(
            boolean meetingCompleted,
            boolean followUpCompleted) {

        return meetingCompleted
                && !followUpCompleted;
    }

    /**
     * Null-safe equality check.
     */
    public static boolean sameMeeting(
            Long firstMeetingId,
            Long secondMeetingId) {

        return Objects.equals(
                firstMeetingId,
                secondMeetingId
        );
    }

}