package com.blueant_crm_erp.meeting.dto.request;

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
public class ScheduleMeetingRequest {

    @NotBlank(message = "Meeting code is required.")
    private String meetingCode;

    @NotNull(message = "Meeting date is required.")
    @FutureOrPresent(message = "Meeting date cannot be in the past.")
    private LocalDate meetingDate;

    @NotNull(message = "Meeting time is required.")
    private LocalTime meetingTime;

    @NotBlank(message = "Meeting location is required.")
    @Size(max = 255, message = "Meeting location cannot exceed 255 characters.")
    private String meetingLocation;

    @Size(max = 500, message = "Agenda cannot exceed 500 characters.")
    private String agenda;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

}