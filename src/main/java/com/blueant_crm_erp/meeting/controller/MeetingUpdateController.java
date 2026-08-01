package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingUpdateResponse;
import com.blueant_crm_erp.meeting.service.MeetingUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * Meeting Update Controller
 * ============================================================================
 *
 * Exposes endpoints for querying the immutable audit history of meeting updates.
 * Updates are never created directly via this controller — they are created
 * implicitly by the workflow engine via POST /{meetingCode}/workflow-update.
 *
 * Endpoint: GET /v1/meetings/{meetingCode}/update-history
 */
@Slf4j
@RestController
@RequestMapping("/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Update API", description = "Endpoints for querying meeting update audit history")
public class MeetingUpdateController {

    private final MeetingUpdateService meetingUpdateService;

    @GetMapping("/{meetingCode}/update-history")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get meeting update audit history",
            description = "Returns all immutable update records for a meeting, ordered by submission sequence."
    )
    public ApiResponse<List<MeetingUpdateResponse>> getUpdateHistory(@PathVariable String meetingCode) {
        log.info("Fetching update history for meeting: {}", meetingCode);
        return ApiResponse.success("Meeting update history retrieved successfully.",
                meetingUpdateService.getUpdateHistory(meetingCode));
    }
}
