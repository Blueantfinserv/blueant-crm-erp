package com.blueant_crm_erp.common.listener;

import com.blueant_crm_erp.common.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationMailTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private JavaMailSender mailSender;

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

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertEquals("Test Subject", sentMessage.getSubject());
        assertEquals("Test Message Body", sentMessage.getText());
    }

    @Test
    public void testEmailNotificationRetryAndRecovery() throws Exception {
        doThrow(new RuntimeException("SMTP Connection Timeout")).when(mailSender).send(any(SimpleMailMessage.class));

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
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }
}
