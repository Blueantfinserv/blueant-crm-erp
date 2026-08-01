package com.blueant_crm_erp.util.lead;

import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for duplicate lead detection.
 *
 * Responsibilities:
 * - Normalize lead identifiers
 * - Compare mobile numbers
 * - Compare email addresses
 * - Compare PAN numbers
 * - Compare company names
 *
 * This utility DOES NOT:
 * - Query database
 * - Check duplicate leads from repository
 * - Apply business rules (40-day transfer rule)
 *
 * Business rules should be handled by LeadService.
 *
 * Used By:
 * - Lead Module
 * - Lead Assignment Module
 * - Duplicate Lead Service
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class DuplicateLeadUtil {

    private DuplicateLeadUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Normalize mobile number.
     *
     * Removes:
     * - Spaces
     * - Hyphens
     * - +91
     * - Brackets
     */
    public static String normalizeMobile(String mobile) {

        if (mobile == null || mobile.isBlank()) {
            return "";
        }

        String normalized = mobile
                .replaceAll("\\s+", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        if (normalized.startsWith("+91")) {
            normalized = normalized.substring(3);
        }

        return normalized;
    }

    /**
     * Normalize email.
     */
    public static String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return "";
        }

        return email.trim()
                .toLowerCase(Locale.ENGLISH);
    }

    /**
     * Normalize PAN.
     */
    public static String normalizePan(String pan) {

        if (pan == null || pan.isBlank()) {
            return "";
        }

        return pan.trim()
                .toUpperCase(Locale.ENGLISH);
    }

    /**
     * Normalize company name.
     */
    public static String normalizeCompany(String company) {

        if (company == null || company.isBlank()) {
            return "";
        }

        return company.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ENGLISH);
    }

    /**
     * Compare mobile numbers.
     */
    public static boolean sameMobile(
            String first,
            String second) {

        return normalizeMobile(first)
                .equals(normalizeMobile(second));
    }

    /**
     * Compare email addresses.
     */
    public static boolean sameEmail(
            String first,
            String second) {

        return normalizeEmail(first)
                .equals(normalizeEmail(second));
    }

    /**
     * Compare PAN numbers.
     */
    public static boolean samePan(
            String first,
            String second) {

        return normalizePan(first)
                .equals(normalizePan(second));
    }

    /**
     * Compare company names.
     */
    public static boolean sameCompany(
            String first,
            String second) {

        return normalizeCompany(first)
                .equals(normalizeCompany(second));
    }

    /**
     * Generic null-safe comparison.
     */
    public static boolean equalsIgnoreCase(
            String first,
            String second) {

        return Objects.equals(
                normalizeEmail(first),
                normalizeEmail(second)
        );
    }

}