package com.blueant_crm_erp.servicerequest.dto.response;

import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private Long id;
    private Long clientId;
    private String srCode;
    private ServiceRequestStatus srStatus;
    private String requestType;
    private BigDecimal investmentAmount;
    private String productType;
    private Long assignedCrmId;
}
