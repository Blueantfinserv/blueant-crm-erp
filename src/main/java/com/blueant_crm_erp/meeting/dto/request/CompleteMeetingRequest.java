package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMeetingRequest {

    @NotBlank(message = "Meeting code is required.")
    private String meetingCode;

    @NotNull(message = "Meeting outcome is required.")
    private MeetingOutcome meetingOutcome;

    @NotBlank(message = "Meeting remarks are required.")
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    /**
     * Set true if lead is converted after this meeting.
     */
    private Boolean leadConverted;

    /**
     * Set true if lead is rejected after this meeting.
     */
    private Boolean leadRejected;

}