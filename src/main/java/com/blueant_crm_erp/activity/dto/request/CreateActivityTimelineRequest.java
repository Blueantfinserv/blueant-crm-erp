package com.blueant_crm_erp.activity.dto.request;

import com.blueant_crm_erp.activity.enums.ActivityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityTimelineRequest {

    @NotNull(message = "Lead ID cannot be null")
    private Long leadId;

    @NotNull(message = "Activity Type cannot be null")
    private ActivityType activityType;

    private Long referenceId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    private Integer sequenceNumber;

    @Size(max = 50, message = "Outcome must not exceed 50 characters")
    private String outcome;

    @Size(max = 50, message = "Previous Status must not exceed 50 characters")
    private String previousStatus;

    @Size(max = 50, message = "Current Status must not exceed 50 characters")
    private String currentStatus;

}
