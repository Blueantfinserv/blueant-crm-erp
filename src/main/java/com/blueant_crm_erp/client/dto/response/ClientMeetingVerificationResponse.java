package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingVerificationResponse {

    private Long id;
    private Long clientMeetingId;
    private String meetingCode;
    private Integer attemptNumber;
    private Boolean isCurrent;
    private String verificationStatus;

    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectionReason;

    private LocalDate newMeetingDate;
    private LocalTime meetingTime;
    private String blueantAppUsed;
    private String aloneWith;
    private String personName;
    private String position;
    private String businessGenerated;
    private String investmentType;
    private BigDecimal investmentAmount;
    private String remarks;
    private LocalDate nextFollowupDate;

    private LocalDateTime createdAt;
    private String createdBy;
}
