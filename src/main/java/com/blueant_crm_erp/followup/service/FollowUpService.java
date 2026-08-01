package com.blueant_crm_erp.followup.service;

import com.blueant_crm_erp.followup.entity.FollowUp;
import java.time.LocalDate;
import java.time.LocalTime;

public interface FollowUpService {
    FollowUp scheduleFollowUp(Long leadId, LocalDate date, LocalTime time, String remarks, String currentUser);
    FollowUp completeFollowUp(Long followUpId, String remarks, LocalDate nextFollowUpDate, LocalTime nextFollowUpTime, String currentUser);
}
