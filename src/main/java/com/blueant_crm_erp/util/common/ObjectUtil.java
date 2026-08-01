package com.blueant_crm_erp.util.common;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for Object operations.
 *
 * Responsibilities:
 * - Null-safe object operations
 * - Object equality
 * - Empty checks
 * - Default value handling
 * - Type checking
 *
 * Used By:
 * - Authentication Module
 * - User Module
 * - Role Module
 * - Lead Module
 * - Client Module
 * - Service Module
 * - Transaction Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ObjectUtil {

    private ObjectUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if object is null.
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * Returns true if object is not null.
     */
    public static boolean isNotNull(Object object) {
        return object != null;
    }

    /**
     * Returns true if both objects are equal.
     */
    public static boolean equals(Object first, Object second) {
        return Objects.equals(first, second);
    }

    /**
     * Returns true if objects are not equal.
     */
    public static boolean notEquals(Object first, Object second) {
        return !Objects.equals(first, second);
    }

    /**
     * Returns default value if object is null.
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Returns Optional of object.
     */
    public static <T> Optional<T> optional(T value) {
        return Optional.ofNullable(value);
    }

    /**
     * Returns true if collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns true if map is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Returns true if string is null or blank.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Returns true if array is null or empty.
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns true if object is instance of given class.
     */
    public static boolean isInstanceOf(
            Object object,
            Class<?> type) {

        Objects.requireNonNull(type, "Type cannot be null.");

        return type.isInstance(object);
    }

    /**
     * Safely cast object.
     */
    public static <T> T cast(
            Object object,
            Class<T> type) {

        Objects.requireNonNull(type, "Target type cannot be null.");

        if (!type.isInstance(object)) {
            return null;
        }

        return type.cast(object);
    }

    /**
     * Returns class name of object.
     */
    public static String className(Object object) {

        if (object == null) {
            return "null";
        }

        return object.getClass().getSimpleName();
    }

    /**
     * Returns identity hash code.
     */
    public static int identityHash(Object object) {
        return System.identityHashCode(object);
    }

}