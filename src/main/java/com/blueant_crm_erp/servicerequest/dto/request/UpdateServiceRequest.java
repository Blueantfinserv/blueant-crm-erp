package com.blueant_crm_erp.servicerequest.dto.request;

import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {

    private ServiceRequestStatus srStatus;

    @Size(max = 50, message = "Request type must not exceed 50 characters")
    private String requestType;

    private BigDecimal investmentAmount;

    @Size(max = 100, message = "Product type must not exceed 100 characters")
    private String productType;

    private Long assignedCrmId;
}
