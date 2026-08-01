package com.blueant_crm_erp.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lead")
@Data
public class LeadProperties {

    /**
     * Days after which an inactive lead can be transferred to new salesperson
     * Business rule: If original SP hasn't contacted in these many days,
     * the duplicate lead goes to the new SP automatically
     * Default: 40 days
     */
    private int duplicateInactiveDays;

    /**
     * Max leads a salesperson can submit per day
     * Prevents spam submissions
     * Default: 30
     */
    private int maxLeadsPerDayPerSm;

    /**
     * Days before nextPlanDate to send follow-up reminder notification
     * Default: 1 day before
     */
    private int followupReminderDays;

    /**
     * Minutes after which a new lead appears in the salesperson's app
     * Matches your current 5-minute delay from Google Sheets script
     * Default: 5 minutes
     */
    private int appVisibleDelayMinutes;
}
