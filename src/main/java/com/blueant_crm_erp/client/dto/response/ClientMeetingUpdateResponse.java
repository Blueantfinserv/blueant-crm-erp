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
public class ClientMeetingUpdateResponse {

    private Long id;
    private Long clientMeetingId;
    private Integer updateNumber;
    private String meetingMode;
    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private String meetingConducted;
    private String clientMeetingStatus;
    private String aloneWith;
    private String personName;
    private String position;
    private String businessGenerated;
    private String remarks;

    // Additional Optional Questions for Client Meeting Update Form
    private String residentialAddress;
    private String officeAddress;
    private String currentCity;
    private String preferredContactTime;
    private String occupationBusinessType;
    private String companyOrganizationName;
    private String communicationPreference;
    private String preferredInvestmentService;
    private String clientRequirements;
    private String otherInfoNotes;

    private LocalDate nextPlanDate;
    private LocalTime nextPlanTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double locationAccuracy;
    private LocalDateTime createdAt;
    private String createdBy;
}
