package com.blueant_crm_erp.util.notification;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email Utility.
 *
 * Centralized utility for email related helper methods.
 *
 * Responsibilities:
 * - Email validation
 * - Email normalization
 * - Domain extraction
 * - Username extraction
 * - Recipient validation
 * - Subject validation
 *
 * This utility DOES NOT:
 * - Send emails
 * - Render templates
 * - Connect to SMTP
 * - Access database
 *
 * Business logic belongs to:
 * - EmailService
 * - NotificationService
 *
 * Used By:
 * - Authentication Module
 * - User Module
 * - Notification Module
 * - Lead Module
 * - Meeting Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class EmailUtil {

    /**
     * RFC compatible email pattern.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    /**
     * Maximum email subject length.
     */
    private static final int MAX_SUBJECT_LENGTH = 255;

    private EmailUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if email is valid.
     */
    public static boolean isValid(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        return EMAIL_PATTERN
                .matcher(email.trim())
                .matches();
    }

    /**
     * Normalize email.
     */
    public static String normalize(String email) {

        Objects.requireNonNull(email, "Email cannot be null.");

        return email.trim().toLowerCase();
    }

    /**
     * Returns username part.
     *
     * john@gmail.com
     * -> john
     */
    public static String getUsername(String email) {

        if (!isValid(email)) {
            return "";
        }

        return normalize(email)
                .split("@")[0];
    }

    /**
     * Returns email domain.
     *
     * john@gmail.com
     * -> gmail.com
     */
    public static String getDomain(String email) {

        if (!isValid(email)) {
            return "";
        }

        return normalize(email)
                .split("@")[1];
    }

    /**
     * Returns true if recipient list exists.
     */
    public static boolean hasRecipients(
            Collection<String> recipients) {

        return recipients != null
                && !recipients.isEmpty();
    }

    /**
     * Returns true if subject is valid.
     */
    public static boolean isValidSubject(String subject) {

        return subject != null
                && !subject.isBlank()
                && subject.length() <= MAX_SUBJECT_LENGTH;
    }

    /**
     * Returns true if body exists.
     */
    public static boolean hasBody(String body) {

        return body != null
                && !body.isBlank();
    }

    /**
     * Returns true if email belongs to company domain.
     */
    public static boolean isCompanyEmail(
            String email,
            String companyDomain) {

        if (!isValid(email)
                || companyDomain == null
                || companyDomain.isBlank()) {
            return false;
        }

        return getDomain(email)
                .equalsIgnoreCase(companyDomain);
    }

}