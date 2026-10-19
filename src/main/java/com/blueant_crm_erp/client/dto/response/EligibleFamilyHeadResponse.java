package com.blueant_crm_erp.client.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleFamilyHeadResponse {
    private Long clientId;
    private String clientCode;
    private String clientName;
    private boolean isCurrentHead;
    private boolean eligible;
    private String eligibilityReason;
    private String qualifyingDetails;
}
