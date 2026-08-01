package com.blueant_crm_erp.util.common;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for String operations.
 *
 * Responsibilities:
 * - Null-safe string operations
 * - Empty/Blank checks
 * - String formatting
 * - String masking
 * - Random string generation
 * - UUID generation
 *
 * Used By:
 * - Authentication Module
 * - User Module
 * - Lead Module
 * - Client Module
 * - Service Module
 * - Transaction Module
 * - Notification Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class StringUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String ALPHA_NUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789";

    private StringUtil() {
        throw new IllegalStateException("Utility class");
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

    /**
     * Returns empty string if value is null.
     */
    public static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns trimmed value.
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Returns uppercase string.
     */
    public static String upper(String value) {
        return isBlank(value)
                ? value
                : value.toUpperCase();
    }

    /**
     * Returns lowercase string.
     */
    public static String lower(String value) {
        return isBlank(value)
                ? value
                : value.toLowerCase();
    }

    /**
     * Capitalizes first character.
     */
    public static String capitalize(String value) {

        if (isBlank(value)) {
            return value;
        }

        return Character.toUpperCase(value.charAt(0))
                + value.substring(1).toLowerCase();
    }

    /**
     * Returns default value if string is blank.
     */
    public static String defaultIfBlank(
            String value,
            String defaultValue) {

        return isBlank(value)
                ? defaultValue
                : value;
    }

    /**
     * Returns true if two strings are equal.
     */
    public static boolean equals(
            String first,
            String second) {

        return Objects.equals(first, second);
    }

    /**
     * Returns true if equal ignoring case.
     */
    public static boolean equalsIgnoreCase(
            String first,
            String second) {

        if (first == null || second == null) {
            return false;
        }

        return first.equalsIgnoreCase(second);
    }

    /**
     * Masks sensitive string.
     *
     * Example:
     * ABCD12345678
     * ->
     * ABCD****5678
     */
    public static String mask(String value) {

        if (isBlank(value) || value.length() <= 8) {
            return value;
        }

        return value.substring(0, 4)
                + "****"
                + value.substring(value.length() - 4);
    }

    /**
     * Generates random alphanumeric string.
     */
    public static String random(int length) {

        if (length <= 0) {
            throw new IllegalArgumentException(
                    "Length must be greater than zero."
            );
        }

        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            builder.append(
                    ALPHA_NUMERIC.charAt(
                            RANDOM.nextInt(ALPHA_NUMERIC.length())
                    )
            );
        }

        return builder.toString();
    }

    /**
     * Generates UUID.
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Joins collection using delimiter.
     */
    public static String join(
            Collection<?> values,
            String delimiter) {

        if (values == null || values.isEmpty()) {
            return "";
        }

        return values.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + delimiter + b)
                .orElse("");
    }

    /**
     * Returns true if string contains text.
     */
    public static boolean contains(
            String source,
            String value) {

        return source != null
                && value != null
                && source.contains(value);
    }

    /**
     * Returns true if string starts with prefix.
     */
    public static boolean startsWith(
            String source,
            String prefix) {

        return source != null
                && prefix != null
                && source.startsWith(prefix);
    }

    /**
     * Returns true if string ends with suffix.
     */
    public static boolean endsWith(
            String source,
            String suffix) {

        return source != null
                && suffix != null
                && source.endsWith(suffix);
    }

}