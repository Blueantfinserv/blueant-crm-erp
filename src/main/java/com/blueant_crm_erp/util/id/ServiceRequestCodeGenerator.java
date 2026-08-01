package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Service Request Codes.
 *
 * Format:
 *
 * BA-SR-2026-000001
 *
 * BA      -> BlueAnt
 * SR      -> Service Request
 * 2026    -> Request Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate Service Request Code
 * - Validate Service Request Code
 * - Extract Year
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation must be handled by SequenceService.
 *
 * Used By:
 * - Service Request Module
 * - CRM Module
 * - Operations Module
 * - Dashboard Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ServiceRequestCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "SR";

    private static final int SEQUENCE_LENGTH = 6;

    private ServiceRequestCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates Service Request Code.
     *
     * Example:
     * BA-SR-2026-000001
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
     * Generates Service Request Code using custom year.
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
     * Returns true if Service Request Code is valid.
     */
    public static boolean isValid(String serviceRequestCode) {

        if (serviceRequestCode == null || serviceRequestCode.isBlank()) {
            return false;
        }

        return serviceRequestCode.matches("^BA-SR-\\d{4}-\\d{6}$");
    }

    /**
     * Validates Service Request Code.
     */
    public static void validate(String serviceRequestCode) {

        Objects.requireNonNull(
                serviceRequestCode,
                "Service Request code cannot be null."
        );

        if (!isValid(serviceRequestCode)) {
            throw new IllegalArgumentException(
                    "Invalid Service Request Code: " + serviceRequestCode
            );
        }
    }

    /**
     * Extracts request year.
     *
     * Example:
     * BA-SR-2026-000125
     * ->
     * 2026
     */
    public static int extractYear(String serviceRequestCode) {

        validate(serviceRequestCode);

        return Integer.parseInt(
                serviceRequestCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * Example:
     * BA-SR-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String serviceRequestCode) {

        validate(serviceRequestCode);

        return Long.parseLong(
                serviceRequestCode.split("-")[3]
        );
    }

}