package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ==============================================================
 * UUID Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating UUID values.
 *
 * Supported Version:
 * - UUID Version 1 to Version 5
 *
 * Features:
 * - Null Safe
 * - Blank Safe
 * - Case Insensitive
 * - Parsing Support
 * - Version Validation
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UUIDValidator {

    /**
     * UUID regex pattern.
     */
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[1-5][0-9a-fA-F]{3}-" +
                    "[89abAB][0-9a-fA-F]{3}-" +
                    "[0-9a-fA-F]{12}$"
    );

    /**
     * Validates UUID.
     *
     * @param uuid UUID string
     * @return true if valid
     */
    public static boolean isValid(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            return false;
        }

        return UUID_PATTERN.matcher(uuid.trim()).matches();
    }

    /**
     * Checks whether UUID can be parsed.
     *
     * @param uuid UUID string
     * @return true if parsable
     */
    public static boolean isParsable(String uuid) {

        if (!isValid(uuid)) {
            return false;
        }

        try {

            UUID.fromString(uuid.trim());
            return true;

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Converts String to UUID.
     *
     * @param uuid UUID string
     * @return UUID object
     * @throws IllegalArgumentException if UUID is invalid
     */
    public static UUID fromString(String uuid) {

        return UUID.fromString(uuid.trim());
    }

    /**
     * Returns normalized UUID.
     *
     * Example:
     * ABCD...
     * ->
     * abcd...
     *
     * @param uuid UUID string
     * @return normalized UUID
     */
    public static String normalize(String uuid) {

        if (!isValid(uuid)) {
            return null;
        }

        return uuid.trim().toLowerCase();
    }

    /**
     * Generates random UUID.
     *
     * @return UUID string
     */
    public static String randomUUID() {

        return UUID.randomUUID().toString();
    }

}