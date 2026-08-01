package com.blueant_crm_erp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler Configuration
 *
 * Enables Spring Scheduling.
 *
 * Used For:
 * - Lead Reminder
 * - Follow-up Reminder
 * - Dashboard Refresh
 * - Cache Cleanup
 * - Report Generation
 * - Notification Scheduler
 * - Duplicate Lead Checker
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig {

    public SchedulerConfig() {
        log.info("Scheduler Configuration Initialized Successfully.");
    }

}