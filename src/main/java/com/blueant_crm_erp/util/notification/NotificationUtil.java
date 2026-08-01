package com.blueant_crm_erp.util.notification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Notification Utility.
 *
 * Centralized utility for notification related helper methods.
 *
 * Responsibilities:
 * - Notification validation
 * - Title validation
 * - Message validation
 * - Recipient validation
 * - Expiry validation
 * - Duplicate recipient removal
 *
 * This utility DOES NOT:
 * - Send notification
 * - Access database
 * - Call Email API
 * - Call SMS API
 * - Call WhatsApp API
 *
 * Business logic belongs to:
 * - NotificationService
 * - EmailService
 * - SmsService
 * * - WhatsAppService
 *
 * Used By:
 * - Authentication Module
 * - Lead Module
 * - Meeting Module
 * - HR Module
 * - Helpdesk Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class NotificationUtil {

    /**
     * Maximum notification title length.
     */
    private static final int MAX_TITLE_LENGTH = 150;

    /**
     * Maximum notification message length.
     */
    private static final int MAX_MESSAGE_LENGTH = 4000;

    private NotificationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if title is valid.
     */
    public static boolean isValidTitle(String title) {

        return title != null
                && !title.isBlank()
                && title.trim().length() <= MAX_TITLE_LENGTH;
    }

    /**
     * Returns true if message is valid.
     */
    public static boolean isValidMessage(String message) {

        return message != null
                && !message.isBlank()
                && message.trim().length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Returns true if notification has recipients.
     */
    public static boolean hasRecipients(
            Collection<?> recipients) {

        return recipients != null
                && !recipients.isEmpty();
    }

    /**
     * Removes duplicate recipients.
     */
    public static <T> Set<T> uniqueRecipients(
            Collection<T> recipients) {

        Objects.requireNonNull(
                recipients,
                "Recipients cannot be null."
        );

        return new HashSet<>(recipients);
    }

    /**
     * Returns true if notification has expiry.
     */
    public static boolean hasExpiry(
            LocalDateTime expiryTime) {

        return expiryTime != null;
    }

    /**
     * Returns true if notification has expired.
     */
    public static boolean isExpired(
            LocalDateTime expiryTime) {

        return expiryTime != null
                && expiryTime.isBefore(LocalDateTime.now());
    }

    /**
     * Returns true if notification is active.
     */
    public static boolean isActive(
            LocalDateTime expiryTime) {

        return expiryTime == null
                || expiryTime.isAfter(LocalDateTime.now());
    }

    /**
     * Returns true if notification can be sent.
     */
    public static boolean canSend(
            String title,
            String message,
            Collection<?> recipients) {

        return isValidTitle(title)
                && isValidMessage(message)
                && hasRecipients(recipients);
    }

    /**
     * Returns true if value exists.
     */
    public static boolean hasValue(String value) {

        return value != null
                && !value.isBlank();
    }

}