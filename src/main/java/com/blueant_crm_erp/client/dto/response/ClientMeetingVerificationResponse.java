package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    private List<String> businessTypes;
    private List<com.blueant_crm_erp.client.dto.ClientBusinessItemDto> businessItems;
    private BigDecimal investmentAmount;
    private String remarks;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextFollowupDate;

    private LocalDateTime createdAt;
    private String createdBy;

    // --- Additive Display & Audit Fields ---
    private Long clientId;
    private String clientCode;
    private String clientName;
    private String clientMobileNumber;
    private Long assignedEmployeeId;
    private String assignedEmployeeCode;
    private String assignedEmployeeName;
    private com.blueant_crm_erp.meeting.enums.SalesRole employeeRole;
    private Integer meetingNumber;
    private String meetingMode;
    private LocalDate meetingDate;
    private LocalTime meetingTimeOfDay;
    private String meetingLocation;
    private String meetingConducted;
    private String clientMeetingStatus;
}
