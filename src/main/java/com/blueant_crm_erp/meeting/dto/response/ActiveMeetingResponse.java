package com.blueant_crm_erp.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Lightweight response returned by GET /v1/meetings/lead/{leadCode}/active.
 *
 * Contains only the meetingCode so the frontend can immediately
 * call the workflow-update API without fetching the full meeting detail.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMeetingResponse {

    /** Business meeting code — use this as the {meetingCode} in the workflow-update API */
    private String meetingCode;
}
