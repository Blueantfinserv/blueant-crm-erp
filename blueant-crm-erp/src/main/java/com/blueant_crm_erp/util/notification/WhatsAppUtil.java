package com.blueant_crm_erp.util.notification;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * WhatsApp Utility.
 *
 * Centralized utility for WhatsApp related helper methods.
 *
 * Responsibilities:
 * - Mobile validation
 * - Mobile normalization
 * - WhatsApp URL generation
 * - Message encoding
 * - Recipient validation
 *
 * This utility DOES NOT:
 * - Send WhatsApp messages
 * - Connect to WhatsApp Business API
 * - Access database
 * - Manage templates
 *
 * Business logic belongs to:
 * - WhatsAppService
 * - NotificationService
 *
 * Used By:
 * - Authentication Module
 * - Lead Module
 * - Meeting Module
 * - Notification Module
 * - Client Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class WhatsAppUtil {

    /**
     * Indian mobile number pattern.
     */
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");

    /**
     * Maximum WhatsApp message length.
     */
    private static final int MAX_MESSAGE_LENGTH = 4096;

    /**
     * WhatsApp Click-to-Chat URL.
     */
    private static final String WHATSAPP_BASE_URL =
            "https://wa.me/";

    private WhatsAppUtil() {
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
     * Removes spaces, hyphens and country prefix.
     *
     * Examples:
     * +91 9876543210
     * 91-9876543210
     * 9876543210
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
     * Returns international mobile format.
     *
     * Example:
     * 9876543210
     * ->
     * 919876543210
     */
    public static String internationalNumber(String mobile) {

        return "91" + normalizeMobile(mobile);
    }

    /**
     * Validates WhatsApp message.
     */
    public static boolean isValidMessage(String message) {

        return message != null
                && !message.isBlank()
                && message.length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * URL encodes message.
     */
    public static String encodeMessage(String message) {

        Objects.requireNonNull(
                message,
                "Message cannot be null."
        );

        return URLEncoder.encode(
                message,
                StandardCharsets.UTF_8
        );
    }

    /**
     * Generates WhatsApp Click-to-Chat URL.
     *
     * Example:
     * https://wa.me/919876543210?text=Hello
     */
    public static String generateChatUrl(
            String mobile,
            String message) {

        if (!isValidMobile(mobile)) {
            throw new IllegalArgumentException(
                    "Invalid mobile number."
            );
        }

        return WHATSAPP_BASE_URL
                + internationalNumber(mobile)
                + "?text="
                + encodeMessage(message);
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
     * Removes duplicate recipients.
     */
    public static Set<String> uniqueRecipients(
            Collection<String> recipients) {

        Objects.requireNonNull(
                recipients,
                "Recipients cannot be null."
        );

        Set<String> result = new HashSet<>();

        for (String mobile : recipients) {

            if (isValidMobile(mobile)) {
                result.add(normalizeMobile(mobile));
            }
        }

        return result;
    }

    /**
     * Returns true if WhatsApp message can be sent.
     */
    public static boolean canSend(
            String mobile,
            String message) {

        return isValidMobile(mobile)
                && isValidMessage(message);
    }

}