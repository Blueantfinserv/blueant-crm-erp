package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
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
    private Boolean meetingConducted;

    private String completedStage;
    private String leadStatus;
    private String clientStatus;
    private String remarks;
    private String joinedMeetingWith;
    private String leaderName;
    private LocalDate nextPlanDate;

    private String panNumber;
    private BigDecimal investmentAmount;
    private String productType;

    private MeetingOutcome meetingOutcome;
    private String discussion;

    private String submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
