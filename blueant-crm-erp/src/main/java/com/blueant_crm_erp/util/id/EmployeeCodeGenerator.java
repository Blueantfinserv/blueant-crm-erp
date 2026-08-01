package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Employee Codes.
 *
 * Format:
 *
 * BA-EMP-2026-000001
 *
 * BA      -> BlueAnt
 * EMP     -> Employee
 * 2026    -> Joining Year
 * 000001  -> Running Sequence
 *
 * Responsibilities:
 * - Generate Employee Code
 * - Validate Employee Code
 * - Extract Year
 * - Extract Sequence
 *
 * NOTE:
 * Sequence generation should be handled by SequenceService.
 *
 * Used By:
 * - User Module
 * - HR Module
 * - Attendance Module
 * - Leave Module
 * - Payroll Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class EmployeeCodeGenerator {

    private static final String PREFIX = "BA";

    private static final String MODULE = "EMP";

    private static final int SEQUENCE_LENGTH = 6;

    private EmployeeCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates Employee Code.
     *
     * Example:
     * BA-EMP-2026-000001
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
     * Generates Employee Code using custom year.
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
     * Returns true if Employee Code is valid.
     */
    public static boolean isValid(String employeeCode) {

        if (employeeCode == null || employeeCode.isBlank()) {
            return false;
        }

        return employeeCode.matches(
                "^BA-EMP-\\d{4}-\\d{6}$"
        );
    }

    /**
     * Extracts joining year.
     *
     * BA-EMP-2026-000001
     * ->
     * 2026
     */
    public static int extractYear(String employeeCode) {

        validate(employeeCode);

        return Integer.parseInt(
                employeeCode.split("-")[2]
        );
    }

    /**
     * Extracts running sequence.
     *
     * BA-EMP-2026-000125
     * ->
     * 125
     */
    public static long extractSequence(String employeeCode) {

        validate(employeeCode);

        return Long.parseLong(
                employeeCode.split("-")[3]
        );
    }

    /**
     * Validates Employee Code.
     */
    public static void validate(String employeeCode) {

        Objects.requireNonNull(
                employeeCode,
                "Employee code cannot be null."
        );

        if (!isValid(employeeCode)) {
            throw new IllegalArgumentException(
                    "Invalid employee code: " + employeeCode
            );
        }
    }

}