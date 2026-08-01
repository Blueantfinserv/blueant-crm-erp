package com.blueant_crm_erp.util.common;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for Boolean operations.
 *
 * Responsibilities:
 * - Null-safe Boolean checks
 * - Boolean conversions
 * - Conditional evaluations
 * - Common boolean helper methods
 *
 * This utility is used throughout the
 * BlueAnt CRM ERP Platform.
 *
 * Example:
 * BooleanUtil.isTrue(value);
 * BooleanUtil.isFalse(value);
 * BooleanUtil.toPrimitive(value);
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class BooleanUtil {

    private BooleanUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true only if value is Boolean.TRUE.
     */
    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    /**
     * Returns true only if value is Boolean.FALSE.
     */
    public static boolean isFalse(Boolean value) {
        return Boolean.FALSE.equals(value);
    }

    /**
     * Returns true if value is null.
     */
    public static boolean isNull(Boolean value) {
        return value == null;
    }

    /**
     * Returns true if value is not null.
     */
    public static boolean isNotNull(Boolean value) {
        return value != null;
    }

    /**
     * Converts nullable Boolean to primitive.
     *
     * Null returns false.
     */
    public static boolean toPrimitive(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    /**
     * Converts nullable Boolean to primitive.
     *
     * Null returns default value.
     */
    public static boolean toPrimitive(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Returns Boolean object.
     */
    public static Boolean valueOf(boolean value) {
        return Boolean.valueOf(value);
    }

    /**
     * Returns logical AND.
     */
    public static boolean and(Boolean first, Boolean second) {
        return isTrue(first) && isTrue(second);
    }

    /**
     * Returns logical OR.
     */
    public static boolean or(Boolean first, Boolean second) {
        return isTrue(first) || isTrue(second);
    }

    /**
     * Returns logical NOT.
     */
    public static boolean not(Boolean value) {
        return !isTrue(value);
    }

    /**
     * Returns true if object is null.
     */
    public static boolean isNull(Object object) {
        return Objects.isNull(object);
    }

    /**
     * Returns true if object is not null.
     */
    public static boolean isNotNull(Object object) {
        return Objects.nonNull(object);
    }

    /**
     * Returns true if collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns true if collection contains data.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Returns true if string is null or blank.
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Returns true if string contains text.
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

}