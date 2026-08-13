package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateMeetingRequest {

    @NotNull(message = "Lead ID is required.")
    private java.util.UUID leadId;

    @NotNull(message = "Meeting mode is required.")
    private MeetingMode meetingMode;

    @NotNull(message = "Meeting date is required.")
    @FutureOrPresent(message = "Meeting date cannot be in the past.")
    private LocalDate meetingDate;

    private LocalTime meetingTime;

    @NotBlank(message = "Meeting location is required.")
    @Size(max = 255, message = "Meeting location cannot exceed 255 characters.")
    private String meetingLocation;

    @Size(max = 500, message = "Agenda cannot exceed 500 characters.")
    private String agenda;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String meetingRemarks;

    @Size(max = 500)
    private String attendees;

    @Size(max = 255)
    private String googleLocation;

    @Size(max = 2000)
    private String discussion;

    @Size(max = 500)
    private String attachment;

    @FutureOrPresent(message = "Next meeting date cannot be in the past.")
    private LocalDate nextMeetingDate;

    private LocalTime nextMeetingTime;

    private com.blueant_crm_erp.meeting.enums.MeetingStatus meetingStatus;

}