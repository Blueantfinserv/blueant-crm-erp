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
public class ClientMeetingResponse {

    private Long id;
    private String meetingCode;
    private Integer meetingNumber;

    private Long clientId;
    private String clientCode;
    private String clientName;

    private Long assignedEmployeeId;
    private String assignedEmployeeCode;
    private String assignedEmployeeName;

    private String meetingMode;
    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private String meetingLocation;

    private String meetingStatus;
    private String meetingConducted;
    private String clientMeetingStatus;

    private String aloneWith;
    private String personName;
    private String position;
    private String businessGenerated;
    private String remarks;

    private LocalDate nextPlanDate;
    private LocalTime nextPlanTime;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double locationAccuracy;

    private Boolean verifiedByPc;
    private String verificationStatus;
    private LocalDateTime createdAt;
}
