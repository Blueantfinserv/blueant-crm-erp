package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * ==============================================================
 * Date Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating dates and date ranges.
 *
 * Supported Formats:
 * - yyyy-MM-dd
 * - dd/MM/yyyy
 * - dd-MM-yyyy
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateValidator {

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter DD_MM_YYYY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DD_MM_YYYY_DASH =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Validates ISO date (yyyy-MM-dd).
     */
    public static boolean isValidIsoDate(String date) {

        return isValid(date, ISO_DATE);
    }

    /**
     * Validates dd/MM/yyyy.
     */
    public static boolean isValidDate(String date) {

        return isValid(date, DD_MM_YYYY);
    }

    /**
     * Validates dd-MM-yyyy.
     */
    public static boolean isValidDashedDate(String date) {

        return isValid(date, DD_MM_YYYY_DASH);
    }

    /**
     * Generic formatter validation.
     */
    public static boolean isValid(String date,
                                  DateTimeFormatter formatter) {

        if (date == null || date.isBlank()) {
            return false;
        }

        try {

            LocalDate.parse(date, formatter);
            return true;

        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    /**
     * Checks whether date is today.
     */
    public static boolean isToday(LocalDate date) {

        return Objects.equals(date, LocalDate.now());
    }

    /**
     * Checks whether date is future.
     */
    public static boolean isFuture(LocalDate date) {

        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks whether date is past.
     */
    public static boolean isPast(LocalDate date) {

        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks date is today or future.
     */
    public static boolean isTodayOrFuture(LocalDate date) {

        return date != null &&
                !date.isBefore(LocalDate.now());
    }

    /**
     * Checks date is today or past.
     */
    public static boolean isTodayOrPast(LocalDate date) {

        return date != null &&
                !date.isAfter(LocalDate.now());
    }

    /**
     * Checks whether start date is before end date.
     */
    public static boolean isValidRange(LocalDate startDate,
                                       LocalDate endDate) {

        if (startDate == null || endDate == null) {
            return false;
        }

        return !startDate.isAfter(endDate);
    }

    /**
     * Checks leap year.
     */
    public static boolean isLeapYear(int year) {

        return LocalDate.of(year, 1, 1)
                .isLeapYear();
    }

    /**
     * Checks valid year-month.
     */
    public static boolean isValidYearMonth(int year,
                                           int month) {

        try {

            YearMonth.of(year, month);
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Checks whether LocalDateTime is in future.
     */
    public static boolean isFuture(LocalDateTime dateTime) {

        return dateTime != null &&
                dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks whether LocalDateTime is in past.
     */
    public static boolean isPast(LocalDateTime dateTime) {

        return dateTime != null &&
                dateTime.isBefore(LocalDateTime.now());
    }

}