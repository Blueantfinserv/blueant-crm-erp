package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * ==============================================================
 * Mobile Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating Indian mobile numbers.
 *
 * Features:
 * - Null Safe
 * - Blank Safe
 * - Country Code Support (+91 / 91)
 * - Normalization
 * - Format Validation
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MobileValidator {

    /**
     * Indian mobile number pattern.
     *
     * Valid Examples:
     * 9876543210
     * +919876543210
     * 919876543210
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^(?:\\+91|91)?[6-9]\\d{9}$"
    );

    /**
     * Expected mobile number length after normalization.
     */
    private static final int MOBILE_LENGTH = 10;

    /**
     * Validates mobile number.
     *
     * @param mobile mobile number
     * @return true if valid
     */
    public static boolean isValid(String mobile) {

        if (mobile == null || mobile.isBlank()) {
            return false;
        }

        String normalizedMobile = normalize(mobile);

        return MOBILE_PATTERN.matcher(normalizedMobile).matches();
    }

    /**
     * Normalizes mobile number.
     *
     * Example:
     * +91 98765-43210
     * ->
     * 9876543210
     *
     * @param mobile mobile number
     * @return normalized mobile
     */
    public static String normalize(String mobile) {

        if (mobile == null) {
            return null;
        }

        String normalized = mobile.replaceAll("[\\s()-]", "");

        if (normalized.startsWith("+91")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("91") && normalized.length() == 12) {
            normalized = normalized.substring(2);
        }

        return normalized;
    }

    /**
     * Checks whether mobile number has valid length.
     *
     * @param mobile mobile number
     * @return true if length is valid
     */
    public static boolean hasValidLength(String mobile) {

        String normalized = normalize(mobile);

        return normalized != null
                && normalized.length() == MOBILE_LENGTH;
    }

    /**
     * Returns masked mobile number.
     *
     * Example:
     * 9876543210
     * ->
     * XXXXXX3210
     *
     * @param mobile mobile number
     * @return masked mobile number
     */
    public static String mask(String mobile) {

        if (!isValid(mobile)) {
            return null;
        }

        String normalized = normalize(mobile);

        return "XXXXXX" + normalized.substring(6);
    }

}