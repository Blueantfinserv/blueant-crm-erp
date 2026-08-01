package com.blueant_crm_erp.followup.service.impl;

import com.blueant_crm_erp.followup.entity.FollowUp;
import com.blueant_crm_erp.followup.repository.FollowUpRepository;
import com.blueant_crm_erp.followup.service.FollowUpService;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;

    @Override
    public FollowUp scheduleFollowUp(Long leadId, LocalDate date, LocalTime time, String remarks, String currentUser) {
        log.info("Scheduling follow-up for lead {} by {}", leadId, currentUser);
        
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Lead ID"));
                
        FollowUp followUp = FollowUp.builder()
                .lead(lead)
                .followupDate(date)
                .followupTime(time)
                .remarks(remarks)
                .status("PENDING")
                .reminder(true)
                .build();
                
        return followUpRepository.save(followUp);
    }

    @Override
    public FollowUp completeFollowUp(Long followUpId, String remarks, LocalDate nextFollowUpDate, LocalTime nextFollowUpTime, String currentUser) {
        log.info("Completing follow-up {} by {}", followUpId, currentUser);
        
        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid FollowUp ID"));
                
        followUp.setStatus("COMPLETED");
        followUp.setRemarks(followUp.getRemarks() + " | Completed Remarks: " + remarks);
        followUp.setCompletedBy(currentUser);
        followUp = followUpRepository.save(followUp);
        
        if (nextFollowUpDate != null && nextFollowUpTime != null) {
            log.info("Auto-scheduling next follow-up for lead {}", followUp.getLead().getId());
            scheduleFollowUp(followUp.getLead().getId(), nextFollowUpDate, nextFollowUpTime, "Auto-scheduled after completion", currentUser);
        }
        
        return followUp;
    }
}
