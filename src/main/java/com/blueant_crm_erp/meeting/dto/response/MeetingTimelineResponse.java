package com.blueant_crm_erp.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingTimelineResponse {

    private Long id;
    private Long leadId;
    private String meetingCode;
    private String eventType;
    private Integer meetingSequence;
    private String meetingOutcome;
    private String previousStatus;
    private String currentStatus;
    private String description;
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

}
