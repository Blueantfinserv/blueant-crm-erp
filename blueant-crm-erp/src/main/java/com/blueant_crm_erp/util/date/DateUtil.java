package com.blueant_crm_erp.util.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility class for LocalDate operations.
 *
 * Responsibilities:
 * - Current date
 * - Date parsing & formatting
 * - Date comparison
 * - Age calculation
 * - Month & Year utilities
 * - Weekend detection
 * - Leap year detection
 *
 * Used By:
 * - User Module
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - Service Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns current date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Format LocalDate.
     */
    public static String format(LocalDate date) {

        Objects.requireNonNull(date, "Date cannot be null.");

        return date.format(DEFAULT_FORMATTER);
    }

    /**
     * Format LocalDate using custom pattern.
     */
    public static String format(
            LocalDate date,
            String pattern) {

        Objects.requireNonNull(date);
        Objects.requireNonNull(pattern);

        return date.format(
                DateTimeFormatter.ofPattern(pattern)
        );
    }

    /**
     * Parse LocalDate.
     */
    public static LocalDate parse(String value) {

        Objects.requireNonNull(value);

        return LocalDate.parse(
                value,
                DEFAULT_FORMATTER
        );
    }

    /**
     * Parse using custom pattern.
     */
    public static LocalDate parse(
            String value,
            String pattern) {

        Objects.requireNonNull(value);
        Objects.requireNonNull(pattern);

        return LocalDate.parse(
                value,
                DateTimeFormatter.ofPattern(pattern)
        );
    }

    /**
     * Calculate age.
     */
    public static int calculateAge(LocalDate birthDate) {

        Objects.requireNonNull(birthDate);

        return Period.between(
                birthDate,
                today()
        ).getYears();
    }

    /**
     * Returns true if leap year.
     */
    public static boolean isLeapYear(LocalDate date) {

        Objects.requireNonNull(date);

        return date.isLeapYear();
    }

    /**
     * Returns true if weekend.
     */
    public static boolean isWeekend(LocalDate date) {

        Objects.requireNonNull(date);

        DayOfWeek day = date.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }

    /**
     * Returns first day of month.
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {

        Objects.requireNonNull(date);

        return date.withDayOfMonth(1);
    }

    /**
     * Returns last day of month.
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {

        Objects.requireNonNull(date);

        return date.withDayOfMonth(
                date.lengthOfMonth()
        );
    }

    /**
     * Returns first day of year.
     */
    public static LocalDate firstDayOfYear(LocalDate date) {

        Objects.requireNonNull(date);

        return LocalDate.of(
                date.getYear(),
                Month.JANUARY,
                1
        );
    }

    /**
     * Returns last day of year.
     */
    public static LocalDate lastDayOfYear(LocalDate date) {

        Objects.requireNonNull(date);

        return LocalDate.of(
                date.getYear(),
                Month.DECEMBER,
                31
        );
    }

    /**
     * Add days.
     */
    public static LocalDate plusDays(
            LocalDate date,
            long days) {

        Objects.requireNonNull(date);

        return date.plusDays(days);
    }

    /**
     * Minus days.
     */
    public static LocalDate minusDays(
            LocalDate date,
            long days) {

        Objects.requireNonNull(date);

        return date.minusDays(days);
    }

    /**
     * Returns total days between two dates.
     */
    public static long daysBetween(
            LocalDate start,
            LocalDate end) {

        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Returns true if date is today.
     */
    public static boolean isToday(LocalDate date) {

        Objects.requireNonNull(date);

        return today().equals(date);
    }

    /**
     * Returns true if future date.
     */
    public static boolean isFuture(LocalDate date) {

        Objects.requireNonNull(date);

        return date.isAfter(today());
    }

    /**
     * Returns true if past date.
     */
    public static boolean isPast(LocalDate date) {

        Objects.requireNonNull(date);

        return date.isBefore(today());
    }

    /**
     * Returns number of days in month.
     */
    public static int daysInMonth(LocalDate date) {

        Objects.requireNonNull(date);

        return YearMonth.from(date).lengthOfMonth();
    }

}