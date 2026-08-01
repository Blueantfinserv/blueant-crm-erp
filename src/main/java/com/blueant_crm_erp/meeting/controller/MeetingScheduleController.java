package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.RescheduleMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.CancelMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Schedule API", description = "Endpoints for scheduling meetings")
public class MeetingScheduleController {

    private final MeetingScheduleService meetingScheduleService;

    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a meeting")
    public ApiResponse<MeetingResponse> scheduleMeeting(@Valid @RequestBody CreateMeetingRequest request, Principal principal) {
        return ApiResponse.success("Meeting scheduled successfully", meetingScheduleService.scheduleMeeting(request, principal.getName()));
    }

    @PostMapping("/{meetingCode}/reschedule")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reschedule a meeting")
    public ApiResponse<MeetingResponse> rescheduleMeeting(@PathVariable String meetingCode,
                                                          @Valid @RequestBody RescheduleMeetingRequest request,
                                                          Principal principal) {
        return ApiResponse.success("Meeting rescheduled successfully", meetingScheduleService.rescheduleMeeting(meetingCode, request, principal.getName()));
    }

    @PostMapping("/{meetingCode}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a meeting")
    public ApiResponse<Void> cancelMeeting(@PathVariable String meetingCode,
                                           @Valid @RequestBody(required = false) CancelMeetingRequest request,
                                           Principal principal) {
        meetingScheduleService.cancelMeeting(meetingCode, principal.getName());
        return ApiResponse.success("Meeting cancelled successfully", null);
    }

    @GetMapping("/today")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get today's meetings")
    public ApiResponse<List<MeetingDetailResponse>> getTodayMeetings() {
        return ApiResponse.success(meetingScheduleService.getTodayMeetings());
    }

    @GetMapping("/upcoming")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get upcoming meetings")
    public ApiResponse<List<MeetingDetailResponse>> getUpcomingMeetings() {
        return ApiResponse.success(meetingScheduleService.getUpcomingMeetings());
    }

    @GetMapping("/by-date")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get meetings by date")
    public ApiResponse<List<MeetingDetailResponse>> getMeetingsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate meetingDate) {
        return ApiResponse.success(meetingScheduleService.getMeetingsByDate(meetingDate));
    }
}
