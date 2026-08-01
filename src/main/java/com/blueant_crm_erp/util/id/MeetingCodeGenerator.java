package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Meeting Codes.
 *
 * Format:
 *
 * BA-MTG-2026-000001
 *
 * BA      -> BlueAnt
 * MTG     -> Meeting
 * 2026    -> Meeting Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate Meeting Code
 * - Validate Meeting Code
 * - Extract Year
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation must be handled by SequenceService.
 *
 * Used By:
 * - Meeting Module
 * - Lead Module
 * - Process Coordinator Module
 * - Dashboard Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class MeetingCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "MTG";

    private static final int SEQUENCE_LENGTH = 6;

    private MeetingCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates Meeting Code.
     *
     * Example:
     * BA-MTG-2026-000001
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
     * Generates Meeting Code using custom year.
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
     * Returns true if Meeting Code is valid.
     */
    public static boolean isValid(String meetingCode) {

        if (meetingCode == null || meetingCode.isBlank()) {
            return false;
        }

        return meetingCode.matches("^BA-MTG-\\d{4}-\\d{6}$");
    }

    /**
     * Validates Meeting Code.
     */
    public static void validate(String meetingCode) {

        Objects.requireNonNull(
                meetingCode,
                "Meeting code cannot be null."
        );

        if (!isValid(meetingCode)) {
            throw new IllegalArgumentException(
                    "Invalid Meeting Code: " + meetingCode
            );
        }
    }

    /**
     * Extracts meeting year.
     *
     * Example:
     * BA-MTG-2026-000125
     * ->
     * 2026
     */
    public static int extractYear(String meetingCode) {

        validate(meetingCode);

        return Integer.parseInt(
                meetingCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * Example:
     * BA-MTG-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String meetingCode) {

        validate(meetingCode);

        return Long.parseLong(
                meetingCode.split("-")[3]
        );
    }

}