package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for a single MeetingUpdate audit record.
 * Immutable once returned — represents a point-in-time snapshot.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateResponse {

    private Long id;
    private String meetingCode;
    private Integer updateNumber;

    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private MeetingMode meetingMode;
    private MeetingConductStatus meetingConducted;

    private String completedStage;
    private MeetingLeadStatus leadStatus;
    private String clientStatus;
    private String remarks;
    private String joinedMeetingWith;
    private String aloneWith;
    private String personName;
    private String position;
    private String leaderName;
    private LocalDate nextPlanDate;

    private String panNumber;
    private BigDecimal investmentAmount;
    private String productType;

    private String reason;
    private LocalTime nextPlanTime;
    private String currentInvestmentCompany;
    private String currentAdvisor;
    private com.blueant_crm_erp.meeting.enums.InvestmentType investmentType;
    private String investmentCompany;
    private String currentStage;
    private String address;

    // GPS fields
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private java.time.LocalDateTime locationCapturedAt;
    private Double locationAccuracy;
    private String googleMapsUrl;
    private String discussion;

    private String submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
