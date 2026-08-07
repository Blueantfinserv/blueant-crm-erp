package com.blueant_crm_erp.meeting.constants;

public final class MeetingConstants {

    private MeetingConstants() {
        throw new IllegalStateException("Utility class");
    }

    /* ==========================================================
     * Module
     * ========================================================== */
    public static final String MODULE_NAME = "Meeting";

    /* ==========================================================
     * API Endpoints
     * ========================================================== */
    public static final String BASE_URL = "/api/v1/meetings";

    /* ==========================================================
     * Code Prefix
     * ========================================================== */
    public static final String MEETING_CODE_PREFIX = "MTG";

    /* ==========================================================
     * Validation Messages
     * ========================================================== */
    public static final String MEETING_NOT_FOUND = "Meeting not found.";

    public static final String MEETING_ALREADY_EXISTS =
            "Meeting already exists.";

    public static final String INVALID_MEETING_ID =
            "Invalid meeting id.";

    public static final String INVALID_LEAD =
            "Lead not found.";

    public static final String INVALID_MEETING_STATUS =
            "Invalid meeting status.";




    public static final String INVALID_MEETING_DATE =
            "Meeting date is invalid.";

    public static final String INVALID_MEETING_TIME =
            "Meeting time is invalid.";

    public static final String MEETING_ALREADY_COMPLETED =
            "Meeting has already been completed.";

    public static final String MEETING_ALREADY_CANCELLED =
            "Meeting has already been cancelled.";

    public static final String MEETING_ALREADY_RESCHEDULED =
            "Meeting has already been rescheduled.";

    public static final String MEETING_ALREADY_CONVERTED =
            "Lead has already been converted.";

    public static final String MEETING_ALREADY_REJECTED =
            "Lead has already been rejected.";

    public static final String MEETING_CANNOT_BE_DELETED =
            "Completed meeting cannot be deleted.";

    public static final String WORKFLOW_NEXT_MEETING_DATE_REQUIRED =
            "Next plan date is required for follow-up scheduling.";

    public static final String WORKFLOW_NEXT_MEETING_DATE_PAST =
            "Next meeting date cannot be in the past.";

    /* ==========================================================
     * Business Rules
     * ========================================================== */

    /**
     * Intro Meeting Number
     */
    public static final Integer INTRO_MEETING_NUMBER = 0;

    /**
     * First Regular Meeting Number
     */
    public static final Integer FIRST_MEETING_NUMBER = 1;

    /**
     * Maximum remarks length
     */
    public static final int MAX_REMARK_LENGTH = 1000;

    /**
     * Maximum agenda length
     */
    public static final int MAX_AGENDA_LENGTH = 500;

    /**
     * Maximum meeting title length
     */
    public static final int MAX_TITLE_LENGTH = 150;

}