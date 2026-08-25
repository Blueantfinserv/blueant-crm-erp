package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BestTimeForMeeting {

    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    FLEXIBLE("Flexible"),
    NOT_DISCLOSED("Not Disclosed");

    private final String displayName;

}
