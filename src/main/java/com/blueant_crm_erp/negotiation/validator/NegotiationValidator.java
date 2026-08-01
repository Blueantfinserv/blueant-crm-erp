package com.blueant_crm_erp.negotiation.validator;

import com.blueant_crm_erp.negotiation.dto.request.UpdateNegotiationRequest;
import com.blueant_crm_erp.negotiation.exception.InvalidNegotiationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NegotiationValidator {

    public void validateUpdateRequest(UpdateNegotiationRequest request) {
        if (request.getAgreedAmount() != null && request.getAgreedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidNegotiationException("Agreed amount cannot be negative");
        }
    }
}
