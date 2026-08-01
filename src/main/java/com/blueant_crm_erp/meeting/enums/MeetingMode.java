package com.blueant_crm_erp.meeting.enums;

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
    ONLINE("Online"),

    /**
     * Telephonic Meeting
     */
    PHONE("Phone");

    private final String displayName;
}