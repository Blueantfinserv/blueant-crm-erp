package com.blueant_crm_erp.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

@Getter
public class ReportingManagerChangedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id
     */
    private final Long userId;

    /**
     * New Reporting Manager Id
     */
    private final Long reportingManagerId;

    /**
     * Updated By User Id
     */
    private final Long updatedBy;

    /**
     * Event Time
     */
    private final LocalDateTime eventTime;

    public ReportingManagerChangedEvent(
            Object source,
            Long userId,
            Long reportingManagerId,
            Long updatedBy
    ) {
        super(source);
        this.userId = userId;
        this.reportingManagerId = reportingManagerId;
        this.updatedBy = updatedBy;
        this.eventTime = LocalDateTime.now();
    }

}