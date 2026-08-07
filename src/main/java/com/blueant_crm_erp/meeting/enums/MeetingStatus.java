package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingStatus {

    /**
     * Meeting has been scheduled.
     */
    SCHEDULED("Scheduled"),

    /**
     * Meeting has been completed successfully.
     */
    COMPLETED("Completed"),

    /**
     * Meeting has been rescheduled.
     */
    RESCHEDULED("Rescheduled"),

    /**
     * Meeting has been cancelled.
     */
    CANCELLED("Cancelled"),

    /**
     * Client did not attend the meeting.
     */
    NO_SHOW("No Show"),

    /**
     * Meeting was not conducted.
     */
    NOT_CONDUCTED("Not Conducted");

    private final String displayName;

}