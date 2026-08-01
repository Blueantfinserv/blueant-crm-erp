package com.blueant_crm_erp.util.meeting;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Utility class for Meeting Scheduling.
 *
 * Responsibilities:
 * - Meeting date validation
 * - Office hour validation
 * - Weekend validation
 * - Duration validation
 * - Future meeting validation
 * - Generic schedule helper methods
 *
 * This utility DOES NOT:
 * - Create meetings
 * - Update meetings
 * - Access database
 * - Check participant availability
 * - Send notifications
 *
 * Business logic belongs to:
 * - MeetingService
 * - MeetingSchedulerService
 *
 * Used By:
 * - Meeting Module
 * - Lead Module
 * - CRM Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class MeetingScheduleUtil {

    /**
     * Office start time.
     */
    private static final LocalTime OFFICE_START = LocalTime.of(9, 0);

    /**
     * Office closing time.
     */
    private static final LocalTime OFFICE_END = LocalTime.of(18, 0);

    /**
     * Minimum meeting duration.
     */
    private static final Duration MIN_DURATION = Duration.ofMinutes(15);

    /**
     * Maximum meeting duration.
     */
    private static final Duration MAX_DURATION = Duration.ofHours(4);

    private MeetingScheduleUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if meeting is scheduled in future.
     */
    public static boolean isFutureMeeting(LocalDateTime meetingDateTime) {

        Objects.requireNonNull(meetingDateTime);

        return meetingDateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Returns true if meeting is today.
     */
    public static boolean isToday(LocalDateTime meetingDateTime) {

        Objects.requireNonNull(meetingDateTime);

        return meetingDateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * Returns true if meeting falls on weekend.
     */
    public static boolean isWeekend(LocalDateTime meetingDateTime) {

        Objects.requireNonNull(meetingDateTime);

        DayOfWeek day = meetingDateTime.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }

    /**
     * Returns true if meeting is during office hours.
     */
    public static boolean isWithinOfficeHours(LocalDateTime meetingDateTime) {

        Objects.requireNonNull(meetingDateTime);

        LocalTime time = meetingDateTime.toLocalTime();

        return !time.isBefore(OFFICE_START)
                && !time.isAfter(OFFICE_END);
    }

    /**
     * Returns true if duration is valid.
     */
    public static boolean isValidDuration(Duration duration) {

        Objects.requireNonNull(duration);

        return !duration.minus(MIN_DURATION).isNegative()
                && !duration.minus(MAX_DURATION).isPositive();
    }

    /**
     * Returns true if end time is after start time.
     */
    public static boolean isValidTimeRange(
            LocalDateTime start,
            LocalDateTime end) {

        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        return end.isAfter(start);
    }

    /**
     * Returns meeting duration.
     */
    public static Duration duration(
            LocalDateTime start,
            LocalDateTime end) {

        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        return Duration.between(start, end);
    }

    /**
     * Checks whether two meetings overlap.
     */
    public static boolean overlaps(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd) {

        Objects.requireNonNull(firstStart);
        Objects.requireNonNull(firstEnd);
        Objects.requireNonNull(secondStart);
        Objects.requireNonNull(secondEnd);

        return firstStart.isBefore(secondEnd)
                && secondStart.isBefore(firstEnd);
    }

    /**
     * Returns true if meeting can be scheduled.
     */
    public static boolean canSchedule(
            LocalDateTime start,
            LocalDateTime end) {

        if (!isFutureMeeting(start)) {
            return false;
        }

        if (!isValidTimeRange(start, end)) {
            return false;
        }

        if (!isWithinOfficeHours(start)
                || !isWithinOfficeHours(end)) {
            return false;
        }

        if (isWeekend(start)) {
            return false;
        }

        return isValidDuration(duration(start, end));
    }

}