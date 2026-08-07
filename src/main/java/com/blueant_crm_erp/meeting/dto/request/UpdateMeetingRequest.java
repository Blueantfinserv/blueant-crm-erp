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
public class UpdateMeetingRequest {

    @NotBlank(message = "Meeting code is required.")
    private String meetingCode;

    @NotNull(message = "Meeting date is required.")
    @FutureOrPresent(message = "Meeting date cannot be in the past.")
    private LocalDate meetingDate;

    @NotNull(message = "Meeting time is required.")
    private LocalTime meetingTime;

    @NotNull(message = "Meeting mode is required.")
    private MeetingMode meetingMode;

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

    @Size(max = 20)
    private String aloneWith;

    @Size(max = 100)
    private String personName;

    @Size(max = 100)
    private String position;

    @Size(max = 2000)
    private String discussion;

    @Size(max = 500)
    private String attachment;

    private com.blueant_crm_erp.meeting.enums.MeetingStatus meetingStatus;

}