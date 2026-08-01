package com.blueant_crm_erp.lead.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ============================================================================
 * Lead Statistics Response
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatisticsResponse {

    private Long totalLeads;
    private Long todayLeads;
    
    // Meeting Statistics
    private Long todayMeetings;
    private Long todayIntroMeetings;
    private Long meetingVerificationStatistics; // Count of verified meetings

    // Workflow Statistics
    private Long todayConverted;
    private Long todayFollowUps;
    private Long todayRemoved;
    private Long todayNotInterested;
    
    // Pending Statistics
    private Long pendingFollowUps;
    private Long pendingServiceRequests;
    
    // Duplicate Statistics
    private Long duplicateLeads;

    // Aggregates
    private Map<String, Long> leaderWiseStatistics;
    private Map<String, Long> salesPersonWiseStatistics;

    // Performance
    private Double conversionPercentage;

}