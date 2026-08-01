package com.blueant_crm_erp.util.id;

import com.github.f4b6a3.ulid.UlidCreator;

import com.github.f4b6a3.ulid.Ulid;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class for ULID generation.
 *
 * ULID (Universally Unique Lexicographically Sortable Identifier)
 *
 * Example:
 * 01J2VQFKDTH4V2P5M7F3W8N9ZX
 *
 * Responsibilities:
 * - Generate ULID
 * - Validate ULID
 * - Extract timestamp
 *
 * Used By:
 * - Internal identifiers
 * - Distributed processing
 * - Audit logs
 * - Event tracking
 *
 * NOTE:
 * This utility is NOT used for business codes.
 *
 * Business codes:
 * BA-LEAD-2026-000001
 * BA-CL-2026-000001
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class UlidGenerator {

    /**
     * ULID validation pattern.
     */
    private static final Pattern ULID_PATTERN =
            Pattern.compile("^[0-9A-HJKMNP-TV-Z]{26}$");

    private UlidGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates a new ULID.
     *
     * Example:
     * 01J2VQFKDTH4V2P5M7F3W8N9ZX
     */
    public static String generate() {
        return UlidCreator.getUlid().toString();
    }

    /**
     * Returns true if ULID is valid.
     */
    public static boolean isValid(String ulid) {

        if (ulid == null || ulid.isBlank()) {
            return false;
        }

        return ULID_PATTERN.matcher(ulid).matches();
    }

    /**
     * Validates ULID.
     */
    public static void validate(String ulid) {

        Objects.requireNonNull(
                ulid,
                "ULID cannot be null."
        );

        if (!isValid(ulid)) {
            throw new IllegalArgumentException(
                    "Invalid ULID: " + ulid
            );
        }
    }

    /**
     * Returns ULID timestamp (milliseconds since epoch).
     */
    public static long timestamp(String ulid) {

        validate(ulid);

        return Ulid.from(ulid)
                .getTime();
    }

}