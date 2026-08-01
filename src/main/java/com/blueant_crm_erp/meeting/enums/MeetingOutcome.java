package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingOutcome {

    /**
     * Meeting hasn't happened yet.
     */
    PENDING("Pending"),

    /**
     * Client is interested.
     */
    INTERESTED("Interested"),

    /**
     * Client requested another meeting.
     */
    FOLLOW_UP_REQUIRED("Follow-up Required"),

    /**
     * Client documents are pending.
     */
    DOCUMENT_PENDING("Document Pending"),

    /**
     * Lead successfully converted.
     */
    CONVERTED("Converted"),

    /**
     * Lead successfully completed workflow.
     */
    SUCCESS("Success"),

    /**
     * Lead rejected.
     */
    REJECTED("Rejected"),

    /**
     * Client is not interested.
     */
    NOT_INTERESTED("Not Interested"),

    /**
     * No response from client.
     */
    NO_RESPONSE("No Response"),

    /**
     * Client is already associated with BlueAnt.
     */
    ALREADY_CLIENT("Already Client"),

    /**
     * Lead removed due to invalid profile.
     */
    REMOVED("Removed");

    private final String displayName;
}