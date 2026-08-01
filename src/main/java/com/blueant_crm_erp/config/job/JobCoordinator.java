package com.blueant_crm_erp.config.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobCoordinator {

    // Runs every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void performDailyFollowUpSweep() {
        log.info("Starting daily follow-up sweep...");
        // Logic to find overdue follow-ups and update status or send reminders
        log.info("Finished daily follow-up sweep.");
    }

    // Runs every 15 minutes to send reminders for upcoming meetings
    @Scheduled(fixedRate = 900000)
    public void sendMeetingReminders() {
        log.debug("Checking for upcoming meetings to send reminders...");
        // Logic to notify salespeople of upcoming meetings
    }
    
    // Runs on the 1st of every month at midnight
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyTargets() {
        log.info("Generating new monthly targets for sales team...");
        // Logic to clone or create new targets for the new month
        log.info("Finished generating monthly targets.");
    }
}
