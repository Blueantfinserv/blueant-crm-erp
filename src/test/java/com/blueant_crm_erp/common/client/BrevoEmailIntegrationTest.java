package com.blueant_crm_erp.common.client;

import com.blueant_crm_erp.common.dto.brevo.BrevoEmailResponse;
import com.blueant_crm_erp.config.properties.BrevoProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
public class BrevoEmailIntegrationTest {

    @Autowired
    private BrevoEmailClient brevoEmailClient;

    @Autowired
    private BrevoProperties brevoProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(brevoEmailClient.getRestTemplate());
        
        // Ensure test configuration contains properties
        brevoProperties.setApiKey("test-api-key");
        brevoProperties.setFromEmail("test-sender@example.com");
        brevoProperties.setFromName("Test Sender");
        brevoProperties.setApiUrl("https://api.brevo.com/v3");
    }

    @Test
    public void testA_SuccessfulBrevoSend() throws Exception {
        BrevoEmailResponse mockResponse = new BrevoEmailResponse("<message-id-1234>");
        
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("api-key", "test-api-key"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.sender.email").value("test-sender@example.com"))
                .andExpect(jsonPath("$.sender.name").value("Test Sender"))
                .andExpect(jsonPath("$.to[0].email").value("recipient@example.com"))
                .andExpect(jsonPath("$.subject").value("Hello Subject"))
                .andExpect(jsonPath("$.htmlContent").value("HTML Content Body"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(mockResponse)));

        BrevoEmailResponse response = brevoEmailClient.sendEmail(
                "recipient@example.com",
                "Hello Subject",
                "HTML Content Body"
        );

        assertNotNull(response);
        assertEquals("<message-id-1234>", response.getMessageId());
        mockServer.verify();
    }

    @Test
    public void testB_Brevo400BadRequest() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withBadRequest().body("Malformed request syntax"));

        assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            brevoEmailClient.sendEmail("recipient@example.com", "Subject", "Body");
        });
        mockServer.verify();
    }

    @Test
    public void testC_Brevo401Unauthorized() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("Invalid API key"));

        assertThrows(HttpClientErrorException.Unauthorized.class, () -> {
            brevoEmailClient.sendEmail("recipient@example.com", "Subject", "Body");
        });
        mockServer.verify();
    }

    @Test
    public void testD_Brevo429RateLimit() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded"));

        assertThrows(HttpClientErrorException.class, () -> {
            brevoEmailClient.sendEmail("recipient@example.com", "Subject", "Body");
        });
        mockServer.verify();
    }

    @Test
    public void testE_Brevo5xxServerError() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError().body("Brevo service internal error"));

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> {
            brevoEmailClient.sendEmail("recipient@example.com", "Subject", "Body");
        });
        mockServer.verify();
    }

    @Test
    public void testF_ConnectionTimeout() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection timed out");
                });

        assertThrows(ResourceAccessException.class, () -> {
            brevoEmailClient.sendEmail("recipient@example.com", "Subject", "Body");
        });
        mockServer.verify();
    }

    @Test
    public void testG_StartupValidationRejectsMissingOrDefaultCredentials() {
        // 1. Test null/blank apiKey
        BrevoProperties blankKeyProps = new BrevoProperties();
        blankKeyProps.setApiKey("");
        blankKeyProps.setFromEmail("test-sender@example.com");
        BrevoEmailClient clientWithBlankKey = new BrevoEmailClient(blankKeyProps);
        assertThrows(IllegalStateException.class, () -> clientWithBlankKey.validateConfig(true));

        // 2. Test default/test apiKey
        BrevoProperties defaultKeyProps = new BrevoProperties();
        defaultKeyProps.setApiKey("test-api-key");
        defaultKeyProps.setFromEmail("test-sender@example.com");
        BrevoEmailClient clientWithDefaultKey = new BrevoEmailClient(defaultKeyProps);
        assertThrows(IllegalStateException.class, () -> clientWithDefaultKey.validateConfig(true));

        // 3. Test default/test fromEmail
        BrevoProperties defaultEmailProps = new BrevoProperties();
        defaultEmailProps.setApiKey("valid-production-key");
        defaultEmailProps.setFromEmail("test@example.com");
        BrevoEmailClient clientWithDefaultEmail = new BrevoEmailClient(defaultEmailProps);
        assertThrows(IllegalStateException.class, () -> clientWithDefaultEmail.validateConfig(true));
    }
}
