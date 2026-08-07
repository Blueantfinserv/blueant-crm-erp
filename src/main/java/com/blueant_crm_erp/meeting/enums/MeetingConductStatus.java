package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingConductStatus {

    CONDUCTED("Conducted"),
    NOT_CONDUCTED("Not Conducted");

    private final String displayName;

}
