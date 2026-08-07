package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingLeadStatus {

    ALREADY_CLIENT("Already Client"),
    CONVERTED_CLIENT("Converted Client"),
    REMOVE_CLIENT("Remove Client"),
    CLIENT_NOT_INTERESTED("Client Not Interested"),
    WORK_IN_PROGRESS("Work In Progress");

    private final String displayName;

}
