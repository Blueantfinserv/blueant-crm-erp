package com.blueant_crm_erp.util.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for Enum operations.
 *
 * Responsibilities:
 * - Parse enum safely
 * - Validate enum values
 * - List enum names
 * - Check enum existence
 * - Case-insensitive enum lookup
 *
 * Used By:
 * - User Module
 * - Role Module
 * - Lead Module
 * - Client Module
 * - Service Module
 * - Transaction Module
 * - Validation Layer
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class EnumUtil {

    private EnumUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns enum by name (case-insensitive).
     */
    public static <E extends Enum<E>> Optional<E> fromString(
            Class<E> enumClass,
            String value) {

        Objects.requireNonNull(enumClass, "Enum class cannot be null.");

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }

    /**
     * Returns enum or default value.
     */
    public static <E extends Enum<E>> E fromString(
            Class<E> enumClass,
            String value,
            E defaultValue) {

        return fromString(enumClass, value)
                .orElse(defaultValue);
    }

    /**
     * Returns true if enum contains value.
     */
    public static <E extends Enum<E>> boolean contains(
            Class<E> enumClass,
            String value) {

        return fromString(enumClass, value).isPresent();
    }

    /**
     * Returns all enum names.
     */
    public static <E extends Enum<E>> List<String> names(
            Class<E> enumClass) {

        Objects.requireNonNull(enumClass, "Enum class cannot be null.");

        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    /**
     * Returns comma-separated enum names.
     */
    public static <E extends Enum<E>> String namesAsString(
            Class<E> enumClass) {

        return String.join(", ", names(enumClass));
    }

    /**
     * Returns all enum values.
     */
    public static <E extends Enum<E>> List<E> values(
            Class<E> enumClass) {

        Objects.requireNonNull(enumClass, "Enum class cannot be null.");

        return Arrays.asList(enumClass.getEnumConstants());
    }

    /**
     * Returns enum count.
     */
    public static <E extends Enum<E>> int size(
            Class<E> enumClass) {

        return enumClass.getEnumConstants().length;
    }

    /**
     * Returns true if enum has no values.
     */
    public static <E extends Enum<E>> boolean isEmpty(
            Class<E> enumClass) {

        return size(enumClass) == 0;
    }

    /**
     * Returns enum names in lowercase.
     */
    public static <E extends Enum<E>> List<String> lowerCaseNames(
            Class<E> enumClass) {

        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList());
    }

    /**
     * Returns enum names in uppercase.
     */
    public static <E extends Enum<E>> List<String> upperCaseNames(
            Class<E> enumClass) {

        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> e.name().toUpperCase())
                .collect(Collectors.toList());
    }

}