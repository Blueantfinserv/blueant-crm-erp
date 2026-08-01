package com.blueant_crm_erp.servicerequest.service;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;

import java.math.BigDecimal;

public interface ServiceRequestWorkflowService {
    ServiceRequest generateServiceRequest(Client client, BigDecimal investmentAmount, String productType, String currentUserEmail);
}
