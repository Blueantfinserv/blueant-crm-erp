package com.blueant_crm_erp.client.enums;

import java.util.*;
import java.util.stream.Collectors;

public enum PcBusinessType {
    // 23 Business Options
    BANK_CHANGE_UPDATE("Bank Change / Update"),
    CAN_CREATE("CAN Create"),
    CHANGE_OF_BROKER("Change of Broker"),
    CHANGE_OF_TAX_STATUS("Change of Tax Status"),
    KYC("KYC"),
    MAIL_ID_UPDATE("Mail ID Update"),
    MINOR_TO_MAJOR("Minor to Major"),
    MOBILE_NUMBER_UPDATE("Mobile Number Update"),
    NAME_CHANGE("Name Change"),
    NOMINEE_CHANGE_UPDATE("Nominee Change / Update"),
    LUMPSUM_SWITCH_CHANGE("Lumpsum Switch / Change"),
    OTHER_TRANSACTION("Other Transaction"),
    PAYZEE_UPDATE("Payzee Update"),
    REDEEM("Redeem"),
    REPORTING("Reporting"),
    SIP_STP_START("SIP STP Start"),
    SIP_STP_STOP("SIP STP Stop"),
    SWITCH("Switch"),
    SWP_START("SWP Start"),
    SWP_STOP("SWP Stop"),
    TRANSMISSION("Transmission"),
    FOLIO_CLUB("Folio Club"),
    JOINT_HOLDING_AOS("Joint Holding (AOS)"),

    // Legacy types retained for backward compatibility with historical records
    SIP("SIP"),
    LUMP_SUM("Lump Sum"),
    BROKER_CHANGE("Change of Broker"),
    SERVICE_REQUEST("Service Request"),
    INSURANCE("Insurance"),
    LOAN("Loan"),
    SHARE("Share");

    private final String displayName;

    PcBusinessType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static final List<String> DISPLAY_OPTIONS = List.of(
            "Bank Change / Update",
            "CAN Create",
            "Change of Broker",
            "Change of Tax Status",
            "KYC",
            "Mail ID Update",
            "Minor to Major",
            "Mobile Number Update",
            "Name Change",
            "Nominee Change / Update",
            "Lumpsum Switch / Change",
            "Other Transaction",
            "Payzee Update",
            "Redeem",
            "Reporting",
            "SIP STP Start",
            "SIP STP Stop",
            "Switch",
            "SWP Start",
            "SWP Stop",
            "Transmission",
            "Folio Club",
            "Joint Holding (AOS)"
    );

    public static final Set<String> SUPPORTED_TYPES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, String> DISPLAY_NAMES = new LinkedHashMap<>();

    static {
        // Map 23 options display names (uppercase) to canonical enum string
        ALIASES.put("BANK CHANGE / UPDATE", "BANK_CHANGE_UPDATE");
        ALIASES.put("BANK CHANGE/UPDATE", "BANK_CHANGE_UPDATE");
        ALIASES.put("BANK CHANGE UPDATE", "BANK_CHANGE_UPDATE");
        ALIASES.put("CAN CREATE", "CAN_CREATE");
        ALIASES.put("CHANGE OF BROKER", "CHANGE_OF_BROKER");
        ALIASES.put("CHANGE OF TAX STATUS", "CHANGE_OF_TAX_STATUS");
        ALIASES.put("KYC", "KYC");
        ALIASES.put("MAIL ID UPDATE", "MAIL_ID_UPDATE");
        ALIASES.put("MINOR TO MAJOR", "MINOR_TO_MAJOR");
        ALIASES.put("MOBILE NUMBER UPDATE", "MOBILE_NUMBER_UPDATE");
        ALIASES.put("NAME CHANGE", "NAME_CHANGE");
        ALIASES.put("NOMINEE CHANGE / UPDATE", "NOMINEE_CHANGE_UPDATE");
        ALIASES.put("NOMINEE CHANGE/UPDATE", "NOMINEE_CHANGE_UPDATE");
        ALIASES.put("NOMINEE CHANGE UPDATE", "NOMINEE_CHANGE_UPDATE");
        ALIASES.put("LUMPSUM SWITCH / CHANGE", "LUMPSUM_SWITCH_CHANGE");
        ALIASES.put("LUMPSUM SWITCH/CHANGE", "LUMPSUM_SWITCH_CHANGE");
        ALIASES.put("LUMPSUM SWITCH CHANGE", "LUMPSUM_SWITCH_CHANGE");
        ALIASES.put("OTHER TRANSACTION", "OTHER_TRANSACTION");
        ALIASES.put("PAYZEE UPDATE", "PAYZEE_UPDATE");
        ALIASES.put("REDEEM", "REDEEM");
        ALIASES.put("REPORTING", "REPORTING");
        ALIASES.put("SIP STP START", "SIP_STP_START");
        ALIASES.put("SIP STP STOP", "SIP_STP_STOP");
        ALIASES.put("SWITCH", "SWITCH");
        ALIASES.put("SWP START", "SWP_START");
        ALIASES.put("SWP STOP", "SWP_STOP");
        ALIASES.put("TRANSMISSION", "TRANSMISSION");
        ALIASES.put("FOLIO CLUB", "FOLIO_CLUB");
        ALIASES.put("JOINT HOLDING (AOS)", "JOINT_HOLDING_AOS");
        ALIASES.put("JOINT HOLDING AOS", "JOINT_HOLDING_AOS");
        ALIASES.put("JOINT HOLDING", "JOINT_HOLDING_AOS");

        // Legacy aliases
        ALIASES.put("NEW SIP", "SIP");
        ALIASES.put("LUMPSUM", "LUMP_SUM");
        ALIASES.put("LUMP SUM", "LUMP_SUM");
        ALIASES.put("NEW BROKER CHANGE", "CHANGE_OF_BROKER");
        ALIASES.put("BROKER CHANGE", "CHANGE_OF_BROKER");
        ALIASES.put("SERVICE REQUEST", "SERVICE_REQUEST");
        ALIASES.put("SHARES", "SHARE");

        // Populate display names from enum values
        for (PcBusinessType type : values()) {
            DISPLAY_NAMES.put(type.name(), type.getDisplayName());
        }
    }

    public boolean isMonetary() {
        return switch (this) {
            case LUMPSUM_SWITCH_CHANGE,
                 REDEEM,
                 SIP_STP_START,
                 SIP_STP_STOP,
                 SWITCH,
                 SWP_START,
                 SWP_STOP,
                 OTHER_TRANSACTION,
                 TRANSMISSION,
                 SIP,
                 LUMP_SUM,
                 INSURANCE,
                 LOAN,
                 SHARE -> true;
            default -> false;
        };
    }

    public static boolean isMonetary(String type) {
        String normalized = normalize(type);
        if (normalized == null) {
            return false;
        }
        try {
            return PcBusinessType.valueOf(normalized).isMonetary();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

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
        if (ALIASES.containsKey(clean)) {
            return ALIASES.get(clean);
        }
        if (SUPPORTED_TYPES.contains(clean)) {
            return clean;
        }
        return clean;
    }

    public static String getDisplayName(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String normalized = normalize(type);
        if (normalized != null && DISPLAY_NAMES.containsKey(normalized)) {
            return DISPLAY_NAMES.get(normalized);
        }
        return type;
    }
}
