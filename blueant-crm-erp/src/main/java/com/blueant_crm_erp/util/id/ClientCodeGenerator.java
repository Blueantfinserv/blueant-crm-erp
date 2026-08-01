package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Client Codes.
 *
 * Format:
 *
 * BA-CL-2026-000001
 *
 * BA  -> BlueAnt
 * CL  -> Client
 * 2026 -> Financial/Current Year
 * 000001 -> Sequence
 *
 * Responsibilities:
 * - Generate Client Code
 * - Validate Client Code
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation must be handled by
 * SequenceService.
 *
 * Used By:
 * - Client Module
 * - CRM Module
 * - Service Request Module
 * - Transaction Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ClientCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "CL";

    private static final int SEQUENCE_LENGTH = 6;

    private ClientCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates client code.
     *
     * Example:
     *
     * BA-CL-2026-000001
     */
    public static String generate(long sequence) {

        if (sequence <= 0) {
            throw new IllegalArgumentException(
                    "Sequence must be greater than zero."
            );
        }

        int year = LocalDate.now(DateTimeUtil.DEFAULT_ZONE)
                .getYear();

        return String.format(
                "%s-%s-%d-%0" + SEQUENCE_LENGTH + "d",
                PREFIX,
                MODULE,
                year,
                sequence
        );
    }

    /**
     * Generates client code using custom year.
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
     * Returns true if code is valid.
     */
    public static boolean isValid(String clientCode) {

        if (clientCode == null || clientCode.isBlank()) {
            return false;
        }

        return clientCode.matches(
                "^BA-CL-\\d{4}-\\d{6}$"
        );
    }

    /**
     * Extracts sequence.
     *
     * BA-CL-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(
            String clientCode) {

        Objects.requireNonNull(clientCode);

        if (!isValid(clientCode)) {
            throw new IllegalArgumentException(
                    "Invalid client code."
            );
        }

        String[] parts = clientCode.split("-");

        return Long.parseLong(parts[3]);
    }

    /**
     * Extracts year.
     */
    public static int extractYear(
            String clientCode) {

        Objects.requireNonNull(clientCode);

        if (!isValid(clientCode)) {
            throw new IllegalArgumentException(
                    "Invalid client code."
            );
        }

        return Integer.parseInt(
                clientCode.split("-")[2]
        );
    }

}