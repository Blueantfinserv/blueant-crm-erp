package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Incentive Codes.
 *
 * Format:
 *
 * BA-INC-2026-000001
 *
 * BA      -> BlueAnt
 * INC     -> Incentive
 * 2026    -> Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate incentive code
 * - Validate incentive code
 * - Extract year
 * - Extract sequence
 *
 * NOTE:
 * Sequence generation must be handled by SequenceService.
 *
 * Used By:
 * - Incentive Module
 * - Transaction Module
 * - Finance Module
 * - Reports
 * - Dashboard
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class IncentiveCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "INC";

    private static final int SEQUENCE_LENGTH = 6;

    private IncentiveCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates incentive code.
     *
     * Example:
     * BA-INC-2026-000001
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
     * Generates incentive code with custom year.
     */
    public static String generate(
            int year,
            long sequence) {

        if (year <= 0) {
            throw new IllegalArgumentException(
                    "Invalid year."
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
     * Returns true if incentive code is valid.
     */
    public static boolean isValid(String incentiveCode) {

        if (incentiveCode == null || incentiveCode.isBlank()) {
            return false;
        }

        return incentiveCode.matches(
                "^BA-INC-\\d{4}-\\d{6}$"
        );
    }

    /**
     * Extracts year.
     *
     * BA-INC-2026-000001
     * ->
     * 2026
     */
    public static int extractYear(String incentiveCode) {

        validate(incentiveCode);

        return Integer.parseInt(
                incentiveCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * BA-INC-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String incentiveCode) {

        validate(incentiveCode);

        return Long.parseLong(
                incentiveCode.split("-")[3]
        );
    }

    /**
     * Validates incentive code.
     */
    public static void validate(String incentiveCode) {

        Objects.requireNonNull(
                incentiveCode,
                "Incentive code cannot be null."
        );

        if (!isValid(incentiveCode)) {
            throw new IllegalArgumentException(
                    "Invalid incentive code: " + incentiveCode
            );
        }
    }

}