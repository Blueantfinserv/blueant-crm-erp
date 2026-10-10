package com.blueant_crm_erp.client.dto.request;

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
public class ClientMeetingVerificationRequest {

    // Conducted fields
    private LocalDate newMeetingDate;

    private LocalTime meetingTime;

    private String blueantAppUsed;

    private String aloneWith;

    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    private String personName;

    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    private String position;

    private String businessGenerated;

    private String investmentType;

    private BigDecimal investmentAmount;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    private LocalDate nextFollowupDate;

    // Not conducted override field
    private LocalDate meetingDate;
}
