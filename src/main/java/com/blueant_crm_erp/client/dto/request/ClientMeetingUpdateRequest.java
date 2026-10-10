package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingUpdateRequest {

    @NotNull(message = "Meeting conducted status is required.")
    private MeetingConductStatus meetingConducted;

    private MeetingMode meetingMode;

    @NotNull(message = "Meeting date is required.")
    private LocalDate meetingDate;

    private LocalTime meetingTime;

    private String clientMeetingStatus;

    private String aloneWith;

    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    private String personName;

    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    private String position;

    private String businessGenerated;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    private LocalDate nextPlanDate;

    private LocalTime nextPlanTime;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Double accuracy;
}
