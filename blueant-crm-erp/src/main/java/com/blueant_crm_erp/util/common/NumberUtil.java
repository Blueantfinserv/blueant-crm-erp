package com.blueant_crm_erp.util.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Utility class for Number operations.
 *
 * Responsibilities:
 * - Null-safe number operations
 * - Number parsing
 * - Number comparison
 * - Percentage calculation
 * - Currency rounding
 *
 * Used By:
 * - User Module
 * - Lead Module
 * - Client Module
 * - Service Module
 * - Transaction Module
 * - Dashboard Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class NumberUtil {

    private NumberUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if number is null or zero.
     */
    public static boolean isZero(Number number) {

        return number == null
                || number.doubleValue() == 0D;
    }

    /**
     * Returns true if number is greater than zero.
     */
    public static boolean isPositive(Number number) {

        return number != null
                && number.doubleValue() > 0D;
    }

    /**
     * Returns true if number is less than zero.
     */
    public static boolean isNegative(Number number) {

        return number != null
                && number.doubleValue() < 0D;
    }

    /**
     * Returns true if number is between min and max.
     */
    public static boolean isBetween(
            Number value,
            Number min,
            Number max) {

        Objects.requireNonNull(value, "Value cannot be null.");
        Objects.requireNonNull(min, "Minimum value cannot be null.");
        Objects.requireNonNull(max, "Maximum value cannot be null.");

        double number = value.doubleValue();

        return number >= min.doubleValue()
                && number <= max.doubleValue();
    }

    /**
     * Safely parse Integer.
     */
    public static Integer toInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Safely parse Long.
     */
    public static Long toLong(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Safely parse Double.
     */
    public static Double toDouble(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Safely parse BigDecimal.
     */
    public static BigDecimal toBigDecimal(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Round decimal value.
     */
    public static BigDecimal round(
            BigDecimal value,
            int scale) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(
                scale,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Calculate percentage.
     */
    public static BigDecimal percentage(
            BigDecimal amount,
            BigDecimal percentage) {

        Objects.requireNonNull(amount, "Amount cannot be null.");
        Objects.requireNonNull(percentage, "Percentage cannot be null.");

        return amount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Compare two numbers.
     */
    public static int compare(
            BigDecimal first,
            BigDecimal second) {

        Objects.requireNonNull(first, "First value cannot be null.");
        Objects.requireNonNull(second, "Second value cannot be null.");

        return first.compareTo(second);
    }

    /**
     * Returns maximum value.
     */
    public static BigDecimal max(
            BigDecimal first,
            BigDecimal second) {

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        return first.max(second);
    }

    /**
     * Returns minimum value.
     */
    public static BigDecimal min(
            BigDecimal first,
            BigDecimal second) {

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        return first.min(second);
    }

}