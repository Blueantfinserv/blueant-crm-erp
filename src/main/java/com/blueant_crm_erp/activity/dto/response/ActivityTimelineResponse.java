package com.blueant_crm_erp.activity.dto.response;

import com.blueant_crm_erp.activity.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTimelineResponse {
    private Long id;
    private Long leadId;
    private ActivityType activityType;
    private Long referenceId;
    private String title;
    private String description;
    private String status;
    private Integer sequenceNumber;
    private String outcome;
    private String previousStatus;
    private String currentStatus;
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
