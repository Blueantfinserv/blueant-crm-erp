package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeetingOutcomeRequest {

    @NotBlank(message = "Meeting code is required.")
    private String meetingCode;

    @NotNull(message = "Meeting outcome is required.")
    private MeetingOutcome meetingOutcome;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String meetingRemarks;

}