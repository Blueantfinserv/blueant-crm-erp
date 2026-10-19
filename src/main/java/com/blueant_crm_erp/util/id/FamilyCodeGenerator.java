package com.blueant_crm_erp.util.id;

import com.blueant_crm_erp.util.date.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for generating Family Codes.
 *
 * Format:
 * BA-FAM-2026-000001
 *
 * BA   -> BlueAnt
 * FAM  -> Family
 * 2026 -> Financial/Current Year
 * 000001 -> Sequence
 */
public final class FamilyCodeGenerator {

    private static final String PREFIX = "BA";
    private static final String MODULE = "FAM";
    private static final int SEQUENCE_LENGTH = 6;

    private FamilyCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates family code.
     * Example: BA-FAM-2026-000001
     */
    public static String generate(long sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero.");
        }

        int year = LocalDate.now(DateTimeUtil.DEFAULT_ZONE).getYear();

        return String.format(
                "%s-%s-%d-%0" + SEQUENCE_LENGTH + "d",
                PREFIX,
                MODULE,
                year,
                sequence
        );
    }

    /**
     * Generates family code using custom year.
     */
    public static String generate(int year, long sequence) {
        if (year <= 0) {
            throw new IllegalArgumentException("Invalid year.");
        }

        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero.");
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
    public static boolean isValid(String familyCode) {
        if (familyCode == null || familyCode.isBlank()) {
            return false;
        }

        return familyCode.matches("^BA-FAM-\\d{4}-\\d{6}$");
    }

    /**
     * Extracts sequence.
     */
    public static long extractSequence(String familyCode) {
        Objects.requireNonNull(familyCode);

        if (!isValid(familyCode)) {
            throw new IllegalArgumentException("Invalid family code.");
        }

        String[] parts = familyCode.split("-");
        return Long.parseLong(parts[3]);
    }

    /**
     * Extracts year.
     */
    public static int extractYear(String familyCode) {
        Objects.requireNonNull(familyCode);

        if (!isValid(familyCode)) {
            throw new IllegalArgumentException("Invalid family code.");
        }

        return Integer.parseInt(familyCode.split("-")[2]);
    }
}
