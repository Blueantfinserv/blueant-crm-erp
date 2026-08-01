package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadMetricsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadMetricsProviderImpl implements LeadMetricsProvider {

    private final LeadRepository leadRepository;

    @Override
    public long getTotalLeads() {
        return leadRepository.count();
    }

    @Override
    public long getTodayLeads() {
        return leadRepository.countByCreatedAtBetween(getStartOfDay(), getEndOfDay());
    }

    @Override
    public long getTodayMeetings() {
        return leadRepository.countByMeetingDateBetween(getStartOfDay(), getEndOfDay());
    }

    @Override
    public long getTodayIntroMeetings() {
        return leadRepository.countByMeetingDateBetweenAndLeadStage(getStartOfDay(), getEndOfDay(), LeadStage.INTRO_MEETING);
    }

    @Override
    public long getTodayConverted() {
        return leadRepository.countByUpdatedAtBetweenAndLeadStatus(getStartOfDay(), getEndOfDay(), LeadStatus.CONVERTED);
    }

    @Override
    public long getTodayFollowUps() {
        return leadRepository.countByUpdatedAtBetweenAndLeadStatus(getStartOfDay(), getEndOfDay(), LeadStatus.FOLLOW_UP_PENDING);
    }

    @Override
    public long getTodayRemoved() {
        return leadRepository.countByUpdatedAtBetweenAndLeadStatus(getStartOfDay(), getEndOfDay(), LeadStatus.REMOVED);
    }

    @Override
    public long getTodayNotInterested() {
        return leadRepository.countByUpdatedAtBetweenAndLeadStatus(getStartOfDay(), getEndOfDay(), LeadStatus.NOT_INTERESTED);
    }

    @Override
    public long getPendingFollowUps() {
        return leadRepository.countPendingFollowUps(getStartOfDay());
    }

    @Override
    public long getPendingServiceRequests() {
        return 0;
    }

    @Override
    public long getDuplicateLeads() {
        return leadRepository.countByDuplicateLeadStatus(DuplicateLeadStatus.DUPLICATE);
    }

    @Override
    public long getTotalConverted() {
        return leadRepository.countByLeadStatus(LeadStatus.CONVERTED);
    }

    @Override
    public Map<String, Long> getLeaderWiseStatistics() {
        return buildAggregateMap(leadRepository.countLeadsByLeader());
    }

    @Override
    public Map<String, Long> getSalesPersonWiseStatistics() {
        return buildAggregateMap(leadRepository.countLeadsBySalesPerson());
    }

    private LocalDateTime getStartOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime getEndOfDay() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    private Map<String, Long> buildAggregateMap(List<Object[]> queryResults) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : queryResults) {
            String name = result[0] != null ? result[0].toString() : "Unassigned";
            Long count = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            map.put(name, count);
        }
        return map;
    }
}
