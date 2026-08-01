package com.blueant_crm_erp.lead.service;

import java.util.Map;

public interface LeadMetricsProvider {

    long getTotalLeads();

    long getTodayLeads();

    long getTodayMeetings();

    long getTodayIntroMeetings();

    long getTodayConverted();

    long getTodayFollowUps();

    long getTodayRemoved();

    long getTodayNotInterested();

    long getPendingFollowUps();

    long getPendingServiceRequests();

    long getDuplicateLeads();
    
    long getTotalConverted();

    Map<String, Long> getLeaderWiseStatistics();

    Map<String, Long> getSalesPersonWiseStatistics();
}
