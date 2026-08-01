package com.blueant_crm_erp.followup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpResponse {
    private Long id;
    private Long leadId;
    private LocalDate followUpDate;
    private LocalTime followUpTime;
    private String status;
    private String remarks;
    private String scheduledBy;
    private Long nextFollowUpId;
}
