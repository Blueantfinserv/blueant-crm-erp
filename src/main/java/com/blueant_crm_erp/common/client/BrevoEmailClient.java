package com.blueant_crm_erp.common.client;

import com.blueant_crm_erp.common.dto.brevo.BrevoEmailRequest;
import com.blueant_crm_erp.common.dto.brevo.BrevoEmailResponse;
import com.blueant_crm_erp.config.properties.BrevoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@Slf4j
public class BrevoEmailClient {

    private final RestTemplate restTemplate;
    private final BrevoProperties brevoProperties;

    public BrevoEmailClient(BrevoProperties brevoProperties) {
        this.brevoProperties = brevoProperties;
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @jakarta.annotation.PostConstruct
    public void validateConfig() {
        validateConfig(false);
    }

    public void validateConfig(boolean force) {
        if (!force && isTestEnvironment()) {
            return;
        }
        if (brevoProperties.getApiKey() == null || brevoProperties.getApiKey().isBlank() || "test-api-key".equals(brevoProperties.getApiKey())) {
            throw new IllegalStateException("Brevo API Key configuration is missing or invalid in production.");
        }
        if (brevoProperties.getFromEmail() == null || brevoProperties.getFromEmail().isBlank() || "test@example.com".equals(brevoProperties.getFromEmail())) {
            throw new IllegalStateException("Brevo From Email configuration is missing or invalid in production.");
        }
    }

    private boolean isTestEnvironment() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public BrevoEmailResponse sendEmail(String recipient, String subject, String htmlContent) {
        log.info("Initiating email send via BREVO. Recipient: {}, Subject: {}", recipient, subject);

        if (brevoProperties.getApiKey() == null || brevoProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("Brevo API Key configuration is missing.");
        }
        if (brevoProperties.getFromEmail() == null || brevoProperties.getFromEmail().isBlank()) {
            throw new IllegalStateException("Brevo From Email configuration is missing.");
        }

        BrevoEmailRequest requestBody = BrevoEmailRequest.builder()
                .sender(new BrevoEmailRequest.Sender(brevoProperties.getFromEmail(), brevoProperties.getFromName()))
                .to(Collections.singletonList(new BrevoEmailRequest.Recipient(recipient)))
                .subject(subject)
                .htmlContent(htmlContent)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoProperties.getApiKey());

        HttpEntity<BrevoEmailRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = brevoProperties.getApiUrl() + "/smtp/email";

        try {
            ResponseEntity<BrevoEmailResponse> response = restTemplate.postForEntity(url, entity, BrevoEmailResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Brevo email sent successfully. Recipient: {}, MessageId: {}", recipient, response.getBody().getMessageId());
                return response.getBody();
            } else {
                log.error("Brevo email send failed. Recipient: {}, Status: {}", recipient, response.getStatusCode().value());
                throw new RuntimeException("Brevo API returned unexpected status code: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Brevo email send failed. Recipient: {}, Status: {}, Response: {}", 
                      recipient, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Brevo email connection timed out or network error. Recipient: {}, Error: {}", recipient, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Brevo email send failed with unexpected error. Recipient: {}, Exception: {}, Message: {}", 
                      recipient, e.getClass().getName(), e.getMessage());
            throw e;
        }
    }
}
