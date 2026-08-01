package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.dto.response.LeadStatisticsResponse;
import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.service.LeadDashboardService;
import com.blueant_crm_erp.lead.service.LeadMetricsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadDashboardServiceImpl implements LeadDashboardService {

    private final LeadMetricsProvider leadMetricsProvider;
    private final LeadMeetingVerificationServiceImpl verificationService; // Future: MeetingVerificationProvider

    @Override
    public LeadStatisticsResponse getDashboardSummary() {
        LeadStatisticsResponse response = new LeadStatisticsResponse();
        
        // Orchestrate metrics collection from core Lead module
        response.setTotalLeads(leadMetricsProvider.getTotalLeads());
        response.setTodayLeads(leadMetricsProvider.getTodayLeads());
        
        response.setTodayMeetings(leadMetricsProvider.getTodayMeetings());
        response.setTodayIntroMeetings(leadMetricsProvider.getTodayIntroMeetings());
        
        // Orchestrate metrics collection from Meeting/Verification module
        response.setMeetingVerificationStatistics((long) verificationService.getVerifiedMeetingsCount());

        // Workflow Statuses
        response.setTodayConverted(leadMetricsProvider.getTodayConverted());
        response.setTodayFollowUps(leadMetricsProvider.getTodayFollowUps());
        response.setTodayRemoved(leadMetricsProvider.getTodayRemoved());
        response.setTodayNotInterested(leadMetricsProvider.getTodayNotInterested());

        // Pending and Duplicates
        response.setPendingFollowUps(leadMetricsProvider.getPendingFollowUps());
        response.setPendingServiceRequests(leadMetricsProvider.getPendingServiceRequests());
        response.setDuplicateLeads(leadMetricsProvider.getDuplicateLeads());

        // Aggregations
        response.setLeaderWiseStatistics(leadMetricsProvider.getLeaderWiseStatistics());
        response.setSalesPersonWiseStatistics(leadMetricsProvider.getSalesPersonWiseStatistics());
        
        // Conversion %
        long totalConverted = leadMetricsProvider.getTotalConverted();
        double conversionPercentage = (response.getTotalLeads() > 0) 
            ? ((double) totalConverted / response.getTotalLeads()) * 100.0 
            : 0.0;
        response.setConversionPercentage(Math.round(conversionPercentage * 100.0) / 100.0);

        return response;
    }
}
