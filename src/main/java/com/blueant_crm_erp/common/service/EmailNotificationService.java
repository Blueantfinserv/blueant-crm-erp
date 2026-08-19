package com.blueant_crm_erp.common.service;

public interface EmailNotificationService {
    /**
     * Send email notification to recipient
     *
     * @param to recipient email
     * @param subject email subject
     * @param body email body (HTML)
     */
    void sendEmail(String to, String subject, String body);
}
