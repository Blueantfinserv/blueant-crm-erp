package com.blueant_crm_erp.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String recipient;
    private final String subject;
    private final String message;
    private final String channel; // EMAIL, SMS, WHATSAPP

    public NotificationEvent(Object source, String recipient, String subject, String message, String channel) {
        super(source);
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.channel = channel;
    }
}
