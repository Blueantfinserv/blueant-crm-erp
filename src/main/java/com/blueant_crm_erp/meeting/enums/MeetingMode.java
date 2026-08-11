package com.blueant_crm_erp.meeting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingMode {

    /**
     * Physical Face-to-Face Meeting
     */
    PHYSICAL("Physical"),

    /**
     * Online Meeting
     */
    @JsonProperty("VIRTUAL/ONLINE")
    ONLINE("Online"),

    /**
     * Telephonic Meeting
     */
    PHONE("Phone");

    private final String displayName;

    @JsonCreator
    public static MeetingMode fromValue(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if ("VIRTUAL/ONLINE".equals(normalized) || "VIRTUAL_ONLINE".equals(normalized) || "ONLINE".equals(normalized) || "VIRTUAL".equals(normalized)) {
            return ONLINE;
        }
        for (MeetingMode mode : MeetingMode.values()) {
            if (mode.name().equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown meeting mode: " + value);
    }
}