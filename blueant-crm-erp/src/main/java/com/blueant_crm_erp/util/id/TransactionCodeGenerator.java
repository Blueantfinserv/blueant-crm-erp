package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Transaction Codes.
 *
 * Format:
 *
 * BA-TXN-2026-000001
 *
 * BA      -> BlueAnt
 * TXN     -> Transaction
 * 2026    -> Transaction Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate Transaction Code
 * - Validate Transaction Code
 * - Extract Year
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation must be handled by SequenceService.
 *
 * Used By:
 * - Transaction Module
 * - Finance Module
 * - Payment Module
 * - Dashboard Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class TransactionCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "TXN";

    private static final int SEQUENCE_LENGTH = 6;

    private TransactionCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates Transaction Code.
     *
     * Example:
     * BA-TXN-2026-000001
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
     * Generates Transaction Code using custom year.
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
     * Returns true if Transaction Code is valid.
     */
    public static boolean isValid(String transactionCode) {

        if (transactionCode == null || transactionCode.isBlank()) {
            return false;
        }

        return transactionCode.matches("^BA-TXN-\\d{4}-\\d{6}$");
    }

    /**
     * Validates Transaction Code.
     */
    public static void validate(String transactionCode) {

        Objects.requireNonNull(
                transactionCode,
                "Transaction code cannot be null."
        );

        if (!isValid(transactionCode)) {
            throw new IllegalArgumentException(
                    "Invalid Transaction Code: " + transactionCode
            );
        }
    }

    /**
     * Extracts transaction year.
     *
     * Example:
     * BA-TXN-2026-000125
     * ->
     * 2026
     */
    public static int extractYear(String transactionCode) {

        validate(transactionCode);

        return Integer.parseInt(
                transactionCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * Example:
     * BA-TXN-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String transactionCode) {

        validate(transactionCode);

        return Long.parseLong(
                transactionCode.split("-")[3]
        );
    }

}