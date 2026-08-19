package com.blueant_crm_erp.common.listener;

import com.blueant_crm_erp.common.event.NotificationEvent;
import com.blueant_crm_erp.common.service.EmailNotificationService;
import com.blueant_crm_erp.exception.common.TransientNotificationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationMailTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private EmailNotificationService emailNotificationService;

    @Test
    public void testEmailNotificationSending() throws Exception {
        NotificationEvent event = new NotificationEvent(
                this,
                "test@example.com",
                "Test Subject",
                "Test Message Body",
                "EMAIL"
        );

        eventPublisher.publishEvent(event);

        // Sleep briefly because the event listener is @Async
        Thread.sleep(1000);

        verify(emailNotificationService, times(1)).sendEmail(
                eq("test@example.com"),
                eq("Test Subject"),
                eq("Test Message Body")
        );
    }

    @Test
    public void testEmailNotificationRetryAndRecovery() throws Exception {
        doThrow(new TransientNotificationException("Transient timeout")).when(emailNotificationService).sendEmail(any(), any(), any());

        NotificationEvent event = new NotificationEvent(
                this,
                "fail@example.com",
                "Retry Subject",
                "Retry Message Body",
                "EMAIL"
        );

        eventPublisher.publishEvent(event);

        // Sleep longer since it has 3 attempts with 2s initial delay
        Thread.sleep(7000);

        // Verify it was attempted 3 times (initial + 2 retries)
        verify(emailNotificationService, times(3)).sendEmail(
                eq("fail@example.com"),
                eq("Retry Subject"),
                eq("Retry Message Body")
        );
    }
}
