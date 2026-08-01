package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingType {

    /**
     * Initial introductory meeting for a new lead.
     */
    INTRO("Introduction"),

    /**
     * Follow-up meeting after the intro.
     */
    FOLLOW_UP("Follow Up");

    private final String displayName;
}
