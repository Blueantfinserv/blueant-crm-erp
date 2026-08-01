package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * ==============================================================
 * Email Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating email addresses.
 *
 * Features:
 * - Null Safe
 * - Blank Safe
 * - RFC Friendly Validation
 * - Normalization Support
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailValidator {

    /**
     * Maximum email length as per RFC.
     */
    private static final int MAX_EMAIL_LENGTH = 254;

    /**
     * Email validation pattern.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validates email address.
     *
     * @param email email address
     * @return true if valid
     */
    public static boolean isValid(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail = normalize(email);

        if (normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }

    /**
     * Returns normalized email.
     *
     * Example:
     * " Test@Example.COM "
     * ->
     * "test@example.com"
     *
     * @param email email
     * @return normalized email
     */
    public static String normalize(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    /**
     * Checks whether email belongs to a specific domain.
     *
     * @param email email
     * @param domain domain
     * @return true if matches
     */
    public static boolean hasDomain(String email, String domain) {

        if (!isValid(email) || domain == null || domain.isBlank()) {
            return false;
        }

        return normalize(email)
                .endsWith("@" + domain.toLowerCase().trim());
    }

    /**
     * Returns email domain.
     *
     * Example:
     * abc@gmail.com
     * ->
     * gmail.com
     *
     * @param email email
     * @return domain or null
     */
    public static String getDomain(String email) {

        if (!isValid(email)) {
            return null;
        }

        return normalize(email)
                .substring(normalize(email).indexOf('@') + 1);
    }

}