package com.blueant_crm_erp.servicerequest.event;

import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ServiceRequestCreatedEvent extends ApplicationEvent {

    private final ServiceRequest serviceRequest;
    private final String description;
    private final String triggeredBy;

    public ServiceRequestCreatedEvent(Object source, ServiceRequest serviceRequest, String description, String triggeredBy) {
        super(source);
        this.serviceRequest = serviceRequest;
        this.description = description;
        this.triggeredBy = triggeredBy;
    }
}
