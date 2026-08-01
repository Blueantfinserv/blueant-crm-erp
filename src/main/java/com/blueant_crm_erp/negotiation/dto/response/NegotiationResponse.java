package com.blueant_crm_erp.negotiation.dto.response;

import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationResponse {

    private Long id;
    private Long proposalId;
    private String negotiationCode;
    private NegotiationStatus negotiationStatus;
    private BigDecimal finalAgreedAmount;
    private String finalProductType;
}
