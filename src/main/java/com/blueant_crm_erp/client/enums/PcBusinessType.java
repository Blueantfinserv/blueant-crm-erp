package com.blueant_crm_erp.client.enums;

import java.util.*;
import java.util.stream.Collectors;

public enum PcBusinessType {
    SIP,
    LUMP_SUM,
    BROKER_CHANGE,
    SERVICE_REQUEST,
    INSURANCE,
    LOAN,
    SHARE;

    public static final Set<String> SUPPORTED_TYPES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Map<String, String> ALIASES = Map.of(
            "NEW SIP", "SIP",
            "LUMPSUM", "LUMP_SUM",
            "NEW BROKER CHANGE", "BROKER_CHANGE"
    );

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String clean = value.trim().toUpperCase();
        return SUPPORTED_TYPES.contains(clean) || ALIASES.containsKey(clean);
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        return ALIASES.getOrDefault(clean, clean);
    }
}
