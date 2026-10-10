package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
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
public class CreateClientMeetingRequest {

    private String clientCode;

    private Long clientId;

    @NotNull(message = "Meeting mode is required.")
    private MeetingMode meetingMode;

    @NotNull(message = "Meeting date is required.")
    private LocalDate meetingDate;

    @NotNull(message = "Meeting time is required.")
    private LocalTime meetingTime;

    @Size(max = 255, message = "Meeting location cannot exceed 255 characters.")
    private String meetingLocation;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;
}
