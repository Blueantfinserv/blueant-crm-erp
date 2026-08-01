package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * ==============================================================
 * Password Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating password strength.
 *
 * Password Policy:
 * - Minimum 8 characters
 * - Maximum 64 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 * - No whitespace allowed
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordValidator {

    /**
     * Minimum password length.
     */
    public static final int MIN_LENGTH = 8;

    /**
     * Maximum password length.
     */
    public static final int MAX_LENGTH = 64;

    /**
     * Password validation regex.
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*\\d)" +
                    "(?=.*[@$!%*?&^#()_+=\\-{}\\[\\]:;\"'<>,./\\\\|`~])" +
                    "[^\\s]{8,64}$"
    );

    /**
     * Validates password.
     *
     * @param password password
     * @return true if valid
     */
    public static boolean isValid(String password) {

        if (password == null || password.isBlank()) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Checks minimum length.
     *
     * @param password password
     * @return true if valid
     */
    public static boolean hasMinimumLength(String password) {

        return password != null
                && password.length() >= MIN_LENGTH;
    }

    /**
     * Checks maximum length.
     *
     * @param password password
     * @return true if valid
     */
    public static boolean hasMaximumLength(String password) {

        return password != null
                && password.length() <= MAX_LENGTH;
    }

    /**
     * Checks uppercase character.
     *
     * @param password password
     * @return true if contains uppercase
     */
    public static boolean hasUpperCase(String password) {

        return password != null
                && password.matches(".*[A-Z].*");
    }

    /**
     * Checks lowercase character.
     *
     * @param password password
     * @return true if contains lowercase
     */
    public static boolean hasLowerCase(String password) {

        return password != null
                && password.matches(".*[a-z].*");
    }

    /**
     * Checks digit.
     *
     * @param password password
     * @return true if contains digit
     */
    public static boolean hasDigit(String password) {

        return password != null
                && password.matches(".*\\d.*");
    }

    /**
     * Checks special character.
     *
     * @param password password
     * @return true if contains special character
     */
    public static boolean hasSpecialCharacter(String password) {

        return password != null
                && password.matches(".*[@$!%*?&^#()_+=\\-{}\\[\\]:;\"'<>,./\\\\|`~].*");
    }

    /**
     * Checks whitespace.
     *
     * @param password password
     * @return true if contains whitespace
     */
    public static boolean hasWhitespace(String password) {

        return password != null
                && password.matches(".*\\s.*");
    }

}