package com.blueant_crm_erp.servicerequest.validator;

import com.blueant_crm_erp.servicerequest.dto.request.UpdateServiceRequest;
import com.blueant_crm_erp.servicerequest.exception.InvalidServiceRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ServiceRequestValidator {

    public void validateUpdateRequest(UpdateServiceRequest request) {
        if (request.getInvestmentAmount() != null && request.getInvestmentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidServiceRequestException("Investment amount cannot be negative");
        }
    }
}
