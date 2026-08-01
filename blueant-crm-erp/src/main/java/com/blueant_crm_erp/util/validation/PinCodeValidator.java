package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * ==============================================================
 * Pin Code Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating Indian PIN Codes.
 *
 * Features:
 * - Null Safe
 * - Blank Safe
 * - Numeric Validation
 * - Length Validation
 * - Normalization Support
 *
 * Valid Example:
 * 110001
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PinCodeValidator {

    /**
     * Expected PIN code length.
     */
    public static final int PIN_CODE_LENGTH = 6;

    /**
     * Indian PIN code pattern.
     * First digit cannot be zero.
     */
    private static final Pattern PIN_CODE_PATTERN =
            Pattern.compile("^[1-9][0-9]{5}$");

    /**
     * Validates Indian PIN code.
     *
     * @param pinCode pin code
     * @return true if valid
     */
    public static boolean isValid(String pinCode) {

        if (pinCode == null || pinCode.isBlank()) {
            return false;
        }

        String normalizedPinCode = normalize(pinCode);

        return PIN_CODE_PATTERN
                .matcher(normalizedPinCode)
                .matches();
    }

    /**
     * Normalizes PIN code.
     *
     * Example:
     * "110 001"
     * ->
     * "110001"
     *
     * @param pinCode pin code
     * @return normalized pin code
     */
    public static String normalize(String pinCode) {

        if (pinCode == null) {
            return null;
        }

        return pinCode.replaceAll("\\s+", "");
    }

    /**
     * Checks PIN code length.
     *
     * @param pinCode pin code
     * @return true if length is valid
     */
    public static boolean hasValidLength(String pinCode) {

        String normalized = normalize(pinCode);

        return normalized != null
                && normalized.length() == PIN_CODE_LENGTH;
    }

    /**
     * Checks whether PIN code contains only digits.
     *
     * @param pinCode pin code
     * @return true if numeric
     */
    public static boolean isNumeric(String pinCode) {

        String normalized = normalize(pinCode);

        return normalized != null
                && normalized.matches("\\d+");
    }

}