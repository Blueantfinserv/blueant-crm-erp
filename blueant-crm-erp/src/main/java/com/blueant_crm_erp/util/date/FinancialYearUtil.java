package com.blueant_crm_erp.util.date;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Objects;

/**
 * Utility class for Indian Financial Year calculations.
 *
 * Indian Financial Year:
 * 1 April -> 31 March
 *
 * Used By:
 * - Dashboard Module
 * - Reports Module
 * - Transaction Module
 * - Incentive Module
 * - Analytics Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class FinancialYearUtil {

    /**
     * Financial Year starts in April.
     */
    private static final Month FINANCIAL_YEAR_START_MONTH = Month.APRIL;

    /**
     * Financial Year ends in March.
     */
    private static final Month FINANCIAL_YEAR_END_MONTH = Month.MARCH;

    private FinancialYearUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns current financial year.
     *
     * Example:
     * 2026-2027
     */
    public static String currentFinancialYear() {
        return financialYear(LocalDate.now(DateTimeUtil.DEFAULT_ZONE));
    }

    /**
     * Returns financial year for given date.
     *
     * Example:
     * 15-May-2026 -> 2026-2027
     * 10-Jan-2026 -> 2025-2026
     */
    public static String financialYear(LocalDate date) {

        Objects.requireNonNull(date, "Date cannot be null.");

        int year = date.getYear();

        if (date.getMonthValue() >= FINANCIAL_YEAR_START_MONTH.getValue()) {
            return year + "-" + (year + 1);
        }

        return (year - 1) + "-" + year;
    }

    /**
     * Returns financial year start date.
     *
     * Example:
     * FY 2026-2027 -> 2026-04-01
     */
    public static LocalDate startDate(LocalDate date) {

        Objects.requireNonNull(date);

        int year = date.getMonthValue() >= 4
                ? date.getYear()
                : date.getYear() - 1;

        return LocalDate.of(
                year,
                FINANCIAL_YEAR_START_MONTH,
                1
        );
    }

    /**
     * Returns financial year end date.
     *
     * Example:
     * FY 2026-2027 -> 2027-03-31
     */
    public static LocalDate endDate(LocalDate date) {

        Objects.requireNonNull(date);

        int year = date.getMonthValue() >= 4
                ? date.getYear() + 1
                : date.getYear();

        return LocalDate.of(
                year,
                FINANCIAL_YEAR_END_MONTH,
                31
        );
    }

    /**
     * Returns true if date belongs to current FY.
     */
    public static boolean isCurrentFinancialYear(LocalDate date) {

        Objects.requireNonNull(date);

        return financialYear(date)
                .equals(currentFinancialYear());
    }

    /**
     * Returns true if given date is first day of FY.
     */
    public static boolean isFinancialYearStart(LocalDate date) {

        Objects.requireNonNull(date);

        return date.getMonth() == Month.APRIL
                && date.getDayOfMonth() == 1;
    }

    /**
     * Returns true if given date is last day of FY.
     */
    public static boolean isFinancialYearEnd(LocalDate date) {

        Objects.requireNonNull(date);

        return date.getMonth() == Month.MARCH
                && date.getDayOfMonth() == 31;
    }

    /**
     * Returns next financial year.
     *
     * Example:
     * 2026-2027 -> 2027-2028
     */
    public static String nextFinancialYear(LocalDate date) {

        Objects.requireNonNull(date);

        int startYear = startDate(date).getYear();

        return (startYear + 1) + "-" + (startYear + 2);
    }

    /**
     * Returns previous financial year.
     *
     * Example:
     * 2026-2027 -> 2025-2026
     */
    public static String previousFinancialYear(LocalDate date) {

        Objects.requireNonNull(date);

        int startYear = startDate(date).getYear();

        return (startYear - 1) + "-" + startYear;
    }

    /**
     * Returns financial year start year.
     *
     * Example:
     * 2026-2027 -> 2026
     */
    public static int startYear(LocalDate date) {

        Objects.requireNonNull(date);

        return startDate(date).getYear();
    }

    /**
     * Returns financial year end year.
     *
     * Example:
     * 2026-2027 -> 2027
     */
    public static int endYear(LocalDate date) {

        Objects.requireNonNull(date);

        return endDate(date).getYear();
    }

    /**
     * Returns total days in financial year.
     */
    public static int totalDays(LocalDate date) {

        return (int) (endDate(date).toEpochDay()
                - startDate(date).toEpochDay() + 1);
    }

    /**
     * Returns true if given year is leap year.
     */
    public static boolean isLeapFinancialYear(LocalDate date) {

        Objects.requireNonNull(date);

        return Year.of(endYear(date)).isLeap();
    }

}