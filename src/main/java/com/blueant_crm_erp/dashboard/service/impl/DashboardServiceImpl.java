package com.blueant_crm_erp.dashboard.service.impl;

import com.blueant_crm_erp.dashboard.dto.DashboardSummaryResponse;
import com.blueant_crm_erp.dashboard.dto.PipelineResponse;
import com.blueant_crm_erp.dashboard.service.DashboardService;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.followup.repository.FollowUpRepository;
import com.blueant_crm_erp.dashboard.specification.DashboardSpecification;
import com.blueant_crm_erp.target.entity.Target;
import com.blueant_crm_erp.target.repository.TargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository leadRepository;
    private final MeetingRepository meetingRepository;
    private final FollowUpRepository followUpRepository;
    private final TargetRepository targetRepository;

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dashboards", key = "'summary_' + #userId")
    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        log.info("Fetching real dashboard summary for user: {}", userId);
        
        LocalDate today = LocalDate.now();
        String currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Use specifications for proper scoping
        long totalLeads = leadRepository.count(DashboardSpecification.belongsToUser(userId));
        long todaysLeads = leadRepository.count(DashboardSpecification.belongsToUser(userId).and(DashboardSpecification.createdBetween(today, today)));
        long todaysMeetings = 0; // Requires MeetingSpecification
        long pendingFollowups = 0; // Requires FollowUpSpecification
        
        long convertedLeads = leadRepository.count(DashboardSpecification.belongsToUser(userId).and((root, query, cb) -> cb.equal(root.get("leadStatus"), LeadStatus.CONVERTED)));
        long activePipeline = totalLeads - convertedLeads - leadRepository.count(DashboardSpecification.belongsToUser(userId).and((root, query, cb) -> cb.in(root.get("leadStatus")).value(java.util.Arrays.asList(LeadStatus.LOST, LeadStatus.REMOVED, LeadStatus.NOT_INTERESTED))));

        Optional<Target> targetOpt = targetRepository.findByUserIdAndTargetMonth(userId, currentMonth);
        BigDecimal monthlyTarget = targetOpt.map(Target::getRevenueTarget).orElse(BigDecimal.ZERO);
        
        // Mock revenue logic - in real scenario, sum from client/investment tables
        BigDecimal revenue = BigDecimal.ZERO; 
        double targetAchievementPercentage = 0.0;
        
        if (monthlyTarget.compareTo(BigDecimal.ZERO) > 0) {
            targetAchievementPercentage = revenue.divide(monthlyTarget, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        }

        return DashboardSummaryResponse.builder()
                .totalLeads(totalLeads)
                .todaysLeads(todaysLeads)
                .todaysMeetings(todaysMeetings)
                .pendingFollowups(pendingFollowups)
                .convertedLeads(convertedLeads)
                .activePipeline(activePipeline)
                .revenue(revenue)
                .monthlyTarget(monthlyTarget)
                .targetAchievementPercentage(targetAchievementPercentage)
                .build();
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dashboards", key = "'pipeline_' + #userId")
    public PipelineResponse getPipelineSummary(Long userId) {
        log.info("Fetching real pipeline summary for user: {}", userId);
        
        return PipelineResponse.builder()
                .newLeads(countLeadStatus(userId, LeadStatus.NEW))
                .inProgress(countLeadStatus(userId, LeadStatus.WORK_IN_PROGRESS))
                .followUp(countLeadStatus(userId, LeadStatus.FOLLOW_UP_PENDING))
                .meeting(countLeadStatus(userId, LeadStatus.MEETING_SCHEDULED))
                .proposal(0) // Need proposal tables for exact count
                .negotiation(0) // Need negotiation tables
                .converted(countLeadStatus(userId, LeadStatus.CONVERTED))
                .alreadyClient(countLeadStatus(userId, LeadStatus.ALREADY_CLIENT))
                .notInterested(countLeadStatus(userId, LeadStatus.NOT_INTERESTED))
                .removed(countLeadStatus(userId, LeadStatus.REMOVED))
                .build();
    }
    
    private long countLeadStatus(Long userId, LeadStatus status) {
        return leadRepository.count(DashboardSpecification.belongsToUser(userId).and((root, query, cb) -> cb.equal(root.get("leadStatus"), status)));
    }
}
