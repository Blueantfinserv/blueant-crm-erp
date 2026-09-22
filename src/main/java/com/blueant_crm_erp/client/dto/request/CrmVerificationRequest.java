package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmVerificationRequest {

    private Boolean kycVerified;

    private Boolean bankDetailsVerified;

    private Boolean documentsVerified;

    private Boolean clientContactConfirmed;

    @Size(max = 20, message = "PAN number cannot exceed 20 characters.")
    private String panNumber;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;
}
