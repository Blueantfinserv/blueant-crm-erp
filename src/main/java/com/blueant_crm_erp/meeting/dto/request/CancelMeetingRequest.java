package com.blueant_crm_erp.meeting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelMeetingRequest {

    @NotNull(message = "Meeting id is required.")
    private Long meetingId;

    @NotBlank(message = "Cancellation reason is required.")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters.")
    private String cancellationReason;

}