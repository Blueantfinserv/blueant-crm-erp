package com.blueant_crm_erp.util.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for Business Day calculations.
 *
 * Responsibilities:
 * - Check working day
 * - Check weekend
 * - Add business days
 * - Subtract business days
 * - Count business days
 * - Skip weekends and holidays
 *
 * Used By:
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - Service Request Module
 * - Helpdesk Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class BusinessDayUtil {

    private BusinessDayUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if date is Saturday or Sunday.
     */
    public static boolean isWeekend(LocalDate date) {

        Objects.requireNonNull(date, "Date cannot be null.");

        DayOfWeek day = date.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }

    /**
     * Returns true if date is a business day.
     */
    public static boolean isBusinessDay(LocalDate date) {
        return isBusinessDay(date, Collections.emptySet());
    }

    /**
     * Returns true if date is a business day
     * considering holidays.
     */
    public static boolean isBusinessDay(
            LocalDate date,
            Collection<LocalDate> holidays) {

        Objects.requireNonNull(date, "Date cannot be null.");

        Set<LocalDate> holidaySet =
                holidays == null
                        ? Collections.emptySet()
                        : new HashSet<>(holidays);

        return !isWeekend(date)
                && !holidaySet.contains(date);
    }

    /**
     * Adds business days.
     */
    public static LocalDate addBusinessDays(
            LocalDate date,
            int businessDays) {

        return addBusinessDays(
                date,
                businessDays,
                Collections.emptySet()
        );
    }

    /**
     * Adds business days while skipping holidays.
     */
    public static LocalDate addBusinessDays(
            LocalDate date,
            int businessDays,
            Collection<LocalDate> holidays) {

        Objects.requireNonNull(date, "Date cannot be null.");

        if (businessDays < 0) {
            throw new IllegalArgumentException(
                    "Business days cannot be negative."
            );
        }

        LocalDate result = date;
        int count = 0;

        while (count < businessDays) {

            result = result.plusDays(1);

            if (isBusinessDay(result, holidays)) {
                count++;
            }
        }

        return result;
    }

    /**
     * Subtract business days.
     */
    public static LocalDate subtractBusinessDays(
            LocalDate date,
            int businessDays) {

        Objects.requireNonNull(date, "Date cannot be null.");

        if (businessDays < 0) {
            throw new IllegalArgumentException(
                    "Business days cannot be negative."
            );
        }

        LocalDate result = date;
        int count = 0;

        while (count < businessDays) {

            result = result.minusDays(1);

            if (isBusinessDay(result)) {
                count++;
            }
        }

        return result;
    }

    /**
     * Counts business days between two dates.
     */
    public static long countBusinessDays(
            LocalDate startDate,
            LocalDate endDate) {

        return countBusinessDays(
                startDate,
                endDate,
                Collections.emptySet()
        );
    }

    /**
     * Counts business days excluding weekends
     * and holidays.
     */
    public static long countBusinessDays(
            LocalDate startDate,
            LocalDate endDate,
            Collection<LocalDate> holidays) {

        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }

        long count = 0;

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {

            if (isBusinessDay(date, holidays)) {
                count++;
            }

            date = date.plusDays(1);
        }

        return count;
    }

}