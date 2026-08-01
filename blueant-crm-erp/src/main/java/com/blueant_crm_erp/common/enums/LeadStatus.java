package com.blueant_crm_erp.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeadStatus {

    NEW("New"),

    ASSIGNED("Assigned"),

    WORK_IN_PROGRESS("Work In Progress"),

    FOLLOW_UP_PENDING("Follow Up Pending"),

    FOLLOW_UP_COMPLETED("Follow Up Completed"),

    MEETING_SCHEDULED("Meeting Scheduled"),

    MEETING_COMPLETED("Meeting Completed"),

    CONVERTED("Converted"),

    ALREADY_CLIENT("Already Client"),

    NOT_INTERESTED("Not Interested"),

    REMOVED("Removed");

    private final String displayName;

}