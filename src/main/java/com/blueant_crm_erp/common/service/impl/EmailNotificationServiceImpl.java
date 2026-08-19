package com.blueant_crm_erp.common.service.impl;

import com.blueant_crm_erp.common.client.BrevoEmailClient;
import com.blueant_crm_erp.common.service.EmailNotificationService;
import com.blueant_crm_erp.exception.common.PermanentNotificationException;
import com.blueant_crm_erp.exception.common.TransientNotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final BrevoEmailClient brevoEmailClient;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            brevoEmailClient.sendEmail(to, subject, body);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                // Rate limit (HTTP 429) is a transient retryable failure
                throw new TransientNotificationException("Brevo API rate limit exceeded: " + e.getMessage(), e);
            }
            // Other HTTP 4xx are permanent authentication/authorization/validation failures
            throw new PermanentNotificationException("Brevo API permanent error: " + e.getMessage() + ", Response: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            // HTTP 5xx is a transient server failure
            throw new TransientNotificationException("Brevo API transient server error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            // Socket timeout/connection timeout/network failure is transient
            throw new TransientNotificationException("Brevo API connection timeout or network failure: " + e.getMessage(), e);
        } catch (Exception e) {
            // Unexpected exceptions are treated as permanent to prevent infinite retries
            throw new PermanentNotificationException("Brevo API unexpected failure: " + e.getMessage(), e);
        }
    }
}
