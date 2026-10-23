package com.blueant_crm_erp.client.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * Client Best Time to Meet (Client-level preference)
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Client Management (Client Assignment / Registration)
 *
 * Description:
 * Represents preferred meeting time windows for external clients.
 * Dedicated strictly to the Client Assignment workflow, completely separate
 * from Lead Assignment's hourly time slots.
 *
 * Canonical values and display labels:
 * 1. BY_APPOINTMENT -> "By Appointment"
 * 2. FIRST_HALF     -> "1st half of the day"
 * 3. SECOND_HALF    -> "2nd half of the day"
 * 4. WEEKENDS_ONLY  -> "Only on weekend"
 * ============================================================================
 */
@Getter
@RequiredArgsConstructor
public enum ClientBestTimeToMeet {

    BY_APPOINTMENT("By Appointment"),
    FIRST_HALF("1st half of the day"),
    SECOND_HALF("2nd half of the day"),
    WEEKENDS_ONLY("Only on weekend");

    private final String displayName;

    public static final Set<String> SUPPORTED_CODES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    public static final Set<String> SUPPORTED_DISPLAY_NAMES = Arrays.stream(values())
            .map(ClientBestTimeToMeet::getDisplayName)
            .collect(Collectors.toUnmodifiableSet());

    public static final Set<String> LEGACY_VALUES = Set.of(
            "9:00 AM - 12:00 PM",
            "12:00 PM - 3:00 PM",
            "3:00 PM - 6:00 PM",
            "6:00 PM - 9:00 PM"
    );

    private static final Map<String, ClientBestTimeToMeet> LOOKUP = new HashMap<>();

    static {
        for (ClientBestTimeToMeet item : values()) {
            LOOKUP.put(item.name().toUpperCase(), item);
            LOOKUP.put(item.getDisplayName().toUpperCase(), item);
        }
        // Aliases & variations for user input tolerance
        LOOKUP.put("1ST HALF", FIRST_HALF);
        LOOKUP.put("FIRST HALF", FIRST_HALF);
        LOOKUP.put("2ND HALF", SECOND_HALF);
        LOOKUP.put("SECOND HALF", SECOND_HALF);
        LOOKUP.put("WEEKEND", WEEKENDS_ONLY);
        LOOKUP.put("WEEKENDS", WEEKENDS_ONLY);
        LOOKUP.put("ONLY ON WEEKENDS", WEEKENDS_ONLY);
        LOOKUP.put("WEEKEND_ONLY", WEEKENDS_ONLY);
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String clean = value.trim().toUpperCase();
        return LOOKUP.containsKey(clean);
    }

    public static boolean isLegacyValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return LEGACY_VALUES.contains(value.trim());
    }

    public static ClientBestTimeToMeet from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        return LOOKUP.get(clean);
    }

    /**
     * Normalizes an accepted input value (canonical enum name or display label)
     * to the agreed display label for persistence.
     * If the input is null or blank, returns null.
     * If the input is a recognized legacy value, preserves it for backward compatibility.
     */
    public static String normalizeToDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        ClientBestTimeToMeet matched = from(value);
        if (matched != null) {
            return matched.getDisplayName();
        }
        if (isLegacyValue(value)) {
            return value.trim();
        }
        return value.trim();
    }
}
