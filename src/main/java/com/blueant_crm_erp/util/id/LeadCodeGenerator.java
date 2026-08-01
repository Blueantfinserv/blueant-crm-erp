package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Lead Codes.
 *
 * Format:
 *
 * BA-LEAD-2026-000001
 *
 * BA      -> BlueAnt
 * LEAD    -> Lead
 * 2026    -> Creation Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate Lead Code
 * - Validate Lead Code
 * - Extract Year
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation must be handled by SequenceService.
 *
 * Used By:
 * - Lead Module
 * - Follow-up Module
 * - Meeting Module
 * - Client Conversion Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class LeadCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "LEAD";

    private static final int SEQUENCE_LENGTH = 6;

    private LeadCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates Lead Code.
     *
     * Example:
     * BA-LEAD-2026-000001
     */
    public static String generate(long sequence) {

        if (sequence <= 0) {
            throw new IllegalArgumentException(
                    "Sequence must be greater than zero."
            );
        }

        int year = LocalDate.now(DateTimeUtil.DEFAULT_ZONE)
                .getYear();

        return generate(year, sequence);
    }

    /**
     * Generates Lead Code using custom year.
     */
    public static String generate(
            int year,
            long sequence) {

        if (year <= 0) {
            throw new IllegalArgumentException(
                    "Year must be greater than zero."
            );
        }

        if (sequence <= 0) {
            throw new IllegalArgumentException(
                    "Sequence must be greater than zero."
            );
        }

        return String.format(
                "%s-%s-%d-%0" + SEQUENCE_LENGTH + "d",
                PREFIX,
                MODULE,
                year,
                sequence
        );
    }

    /**
     * Returns true if Lead Code is valid.
     */
    public static boolean isValid(String leadCode) {

        if (leadCode == null || leadCode.isBlank()) {
            return false;
        }

        return leadCode.matches("^BA-LEAD-\\d{4}-\\d{6}$");
    }

    /**
     * Validates Lead Code.
     */
    public static void validate(String leadCode) {

        Objects.requireNonNull(
                leadCode,
                "Lead code cannot be null."
        );

        if (!isValid(leadCode)) {
            throw new IllegalArgumentException(
                    "Invalid Lead Code: " + leadCode
            );
        }
    }

    /**
     * Extracts creation year.
     *
     * Example:
     * BA-LEAD-2026-000125
     * ->
     * 2026
     */
    public static int extractYear(String leadCode) {

        validate(leadCode);

        return Integer.parseInt(
                leadCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * Example:
     * BA-LEAD-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String leadCode) {

        validate(leadCode);

        return Long.parseLong(
                leadCode.split("-")[3]
        );
    }

}