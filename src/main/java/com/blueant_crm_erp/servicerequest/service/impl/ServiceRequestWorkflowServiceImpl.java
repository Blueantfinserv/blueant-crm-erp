package com.blueant_crm_erp.servicerequest.service.impl;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import com.blueant_crm_erp.servicerequest.event.ServiceRequestCreatedEvent;
import com.blueant_crm_erp.servicerequest.repository.ServiceRequestRepository;
import com.blueant_crm_erp.servicerequest.service.ServiceRequestWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceRequestWorkflowServiceImpl implements ServiceRequestWorkflowService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ServiceRequest generateServiceRequest(Client client, BigDecimal investmentAmount, String productType, String currentUserEmail) {
        log.info("Generating Service Request for client: {}, by: {}", client.getClientCode(), currentUserEmail);

        if (serviceRequestRepository.findByClientId(client.getId()).isPresent()) {
            log.warn("Service Request already exists for client: {}", client.getClientCode());
            return serviceRequestRepository.findByClientId(client.getId()).get();
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .client(client)
                .srCode("SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .srStatus(ServiceRequestStatus.OPEN)
                .requestType("ONBOARDING")
                .investmentAmount(investmentAmount)
                .productType(productType)
                .assignedCrm(client.getLead().getAssignedSalesPerson()) // Auto assign to the Sales Person for now
                .build();

        ServiceRequest savedSr = serviceRequestRepository.save(serviceRequest);

        eventPublisher.publishEvent(new ServiceRequestCreatedEvent(this, savedSr, "Service Request generated.", currentUserEmail));

        return savedSr;
    }
}
