package com.blueant_crm_erp.common.base;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Event
 *
 * Parent class for all application events.
 *
 * Used By:
 * - User Module
 * - Role Module
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - Transaction Module
 * - Notification Module
 * - Audit Module
 */
@Getter
@ToString
public abstract class BaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique Event Id
     */
    private final String eventId;

    /**
     * Event Creation Time
     */
    private final LocalDateTime eventTime;

    /**
     * Event Source
     */
    private final Object source;

    protected BaseEvent(Object source) {
        this.source = source;
        this.eventId = UUID.randomUUID().toString();
        this.eventTime = LocalDateTime.now();
    }

}