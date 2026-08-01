package com.blueant_crm_erp.util.date;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility class for Date-Time operations.
 *
 * Responsibilities:
 * - Current date & time
 * - Formatting
 * - Parsing
 * - Time difference
 * - Date calculations
 * - Zone conversion
 *
 * Default Time Zone:
 * Asia/Kolkata
 *
 * Used By:
 * - Authentication Module
 * - Audit Module
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - Service Request Module
 * - Transaction Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class DateTimeUtil {

    /**
     * Default application zone.
     */
    public static final ZoneId DEFAULT_ZONE =
            ZoneId.of("Asia/Kolkata");

    /**
     * Default formatter.
     */
    public static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Current LocalDateTime.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Current LocalDate.
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }

    /**
     * Current LocalTime.
     */
    public static LocalTime currentTime() {
        return LocalTime.now(DEFAULT_ZONE);
    }

    /**
     * Current Instant.
     */
    public static Instant instant() {
        return Instant.now();
    }

    /**
     * Current ZonedDateTime.
     */
    public static ZonedDateTime zonedNow() {
        return ZonedDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Format LocalDateTime.
     */
    public static String format(LocalDateTime dateTime) {

        Objects.requireNonNull(dateTime, "DateTime cannot be null.");

        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Format LocalDateTime using custom pattern.
     */
    public static String format(
            LocalDateTime dateTime,
            String pattern) {

        Objects.requireNonNull(dateTime);
        Objects.requireNonNull(pattern);

        return dateTime.format(
                DateTimeFormatter.ofPattern(pattern)
        );
    }

    /**
     * Parse LocalDateTime.
     */
    public static LocalDateTime parse(String value) {

        Objects.requireNonNull(value);

        return LocalDateTime.parse(
                value,
                DEFAULT_FORMATTER
        );
    }

    /**
     * Parse LocalDateTime using pattern.
     */
    public static LocalDateTime parse(
            String value,
            String pattern) {

        Objects.requireNonNull(value);
        Objects.requireNonNull(pattern);

        return LocalDateTime.parse(
                value,
                DateTimeFormatter.ofPattern(pattern)
        );
    }

    /**
     * Add days.
     */
    public static LocalDateTime plusDays(
            LocalDateTime dateTime,
            long days) {

        return dateTime.plusDays(days);
    }

    /**
     * Add hours.
     */
    public static LocalDateTime plusHours(
            LocalDateTime dateTime,
            long hours) {

        return dateTime.plusHours(hours);
    }

    /**
     * Add minutes.
     */
    public static LocalDateTime plusMinutes(
            LocalDateTime dateTime,
            long minutes) {

        return dateTime.plusMinutes(minutes);
    }

    /**
     * Minus days.
     */
    public static LocalDateTime minusDays(
            LocalDateTime dateTime,
            long days) {

        return dateTime.minusDays(days);
    }

    /**
     * Minutes between.
     */
    public static long minutesBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Hours between.
     */
    public static long hoursBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Days between.
     */
    public static long daysBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Check whether date is in future.
     */
    public static boolean isFuture(LocalDateTime dateTime) {

        return dateTime.isAfter(now());
    }

    /**
     * Check whether date is in past.
     */
    public static boolean isPast(LocalDateTime dateTime) {

        return dateTime.isBefore(now());
    }

    /**
     * Convert UTC to IST.
     */
    public static LocalDateTime utcToIst(LocalDateTime utcTime) {

        Objects.requireNonNull(utcTime);

        return utcTime
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(DEFAULT_ZONE)
                .toLocalDateTime();
    }

    /**
     * Convert IST to UTC.
     */
    public static LocalDateTime istToUtc(LocalDateTime istTime) {

        Objects.requireNonNull(istTime);

        return istTime
                .atZone(DEFAULT_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

}