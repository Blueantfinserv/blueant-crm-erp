package com.blueant_crm_erp.common.listener;

import com.blueant_crm_erp.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    @Value("${app.mail.from}")
    private String mailFrom;

    private final JavaMailSender mailSender;

    @Async
    @EventListener
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Processing notification event for: {} via channel: {}", event.getRecipient(), event.getChannel());
        
        if ("EMAIL".equalsIgnoreCase(event.getChannel())) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getRecipient());
            message.setSubject(event.getSubject());
            message.setText(event.getMessage());
            message.setFrom(mailFrom);
            
            if (mailSender instanceof JavaMailSenderImpl javaMailSender) {
                log.info("Resolved SMTP configuration - Host: {}, Port: {}, Username: {}, MailSender initialized: true",
                        javaMailSender.getHost(),
                        javaMailSender.getPort(),
                        (javaMailSender.getUsername() != null && !javaMailSender.getUsername().isBlank()) ? "PRESENT" : "ABSENT");
            } else {
                log.info("SMTP MailSender is active (custom implementation: {})", mailSender.getClass().getName());
            }

            try {
                mailSender.send(message);
                log.info("SMTP send success: email sent successfully to recipient: {}", event.getRecipient());
            } catch (Exception ex) {
                log.error("SMTP send failed. Exception type: {}, message: {}", ex.getClass().getName(), ex.getMessage());
                throw ex; // Re-throw to allow Spring Retry to process
            }
        } else {
            log.info("Mock SMS/WhatsApp sent to {}", event.getRecipient());
        }
    }

    @Recover
    public void recover(Exception e, NotificationEvent event) {
        log.error("Dead Letter: Failed to send notification to {} after retries. Reason: {}", event.getRecipient(), e.getMessage());
        // Logic to persist to a dead_letter_notifications table
    }
}
