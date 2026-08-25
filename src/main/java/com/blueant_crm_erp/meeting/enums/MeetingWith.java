package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingWith {

    SELF("Alone / Self"),
    SOMEONE_ELSE("With Someone");

    private final String displayName;

}
