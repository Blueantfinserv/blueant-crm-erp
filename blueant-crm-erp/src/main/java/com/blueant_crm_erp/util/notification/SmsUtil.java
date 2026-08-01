package com.blueant_crm_erp.util.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SMS Utility.
 *
 * Centralized utility for SMS related helper methods.
 *
 * Responsibilities:
 * - Mobile number validation
 * - Mobile normalization
 * - Country code handling
 * - SMS length validation
 * - Duplicate recipient removal
 *
 * This utility DOES NOT:
 * - Send SMS
 * - Connect to SMS Gateway
 * - Access database
 *
 * Business logic belongs to:
 * - SmsService
 * - NotificationService
 *
 * Used By:
 * - Authentication Module
 * - User Module
 * - Lead Module
 * - Meeting Module
 * * - HR Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class SmsUtil {

    /**
     * Indian mobile number pattern.
     */
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");

    /**
     * Maximum SMS length.
     */
    private static final int MAX_SMS_LENGTH = 160;

    private SmsUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates Indian mobile number.
     */
    public static boolean isValidMobile(String mobile) {

        if (mobile == null || mobile.isBlank()) {
            return false;
        }

        return MOBILE_PATTERN
                .matcher(normalizeMobile(mobile))
                .matches();
    }

    /**
     * Normalizes mobile number.
     *
     * Example:
     * +91 9876543210
     * -> 9876543210
     */
    public static String normalizeMobile(String mobile) {

        Objects.requireNonNull(
                mobile,
                "Mobile number cannot be null."
        );

        String value = mobile
                .replaceAll("\\s+", "")
                .replace("-", "");

        if (value.startsWith("+91")) {
            value = value.substring(3);
        }

        if (value.startsWith("91") && value.length() == 12) {
            value = value.substring(2);
        }

        return value;
    }

    /**
     * Returns mobile with +91 prefix.
     */
    public static String withCountryCode(String mobile) {

        String normalized = normalizeMobile(mobile);

        return "+91" + normalized;
    }

    /**
     * Validates SMS body.
     */
    public static boolean isValidMessage(String message) {

        return message != null
                && !message.isBlank()
                && message.length() <= MAX_SMS_LENGTH;
    }

    /**
     * Checks recipient list.
     */
    public static boolean hasRecipients(
            Collection<String> recipients) {

        return recipients != null
                && !recipients.isEmpty();
    }

    /**
     * Removes duplicate mobile numbers.
     */
    public static Set<String> uniqueRecipients(
            Collection<String> recipients) {

        Objects.requireNonNull(
                recipients,
                "Recipients cannot be null."
        );

        Set<String> mobiles = new HashSet<>();

        for (String mobile : recipients) {

            if (isValidMobile(mobile)) {
                mobiles.add(normalizeMobile(mobile));
            }
        }

        return mobiles;
    }

    /**
     * Returns true if SMS can be sent.
     */
    public static boolean canSend(
            String mobile,
            String message) {

        return isValidMobile(mobile)
                && isValidMessage(message);
    }

}