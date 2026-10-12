package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDocumentResponse {

    private Long id;
    private ClientDocumentType documentType;
    private String documentName;
    private Boolean received;
    private LocalDate receivedDate;
    private VerificationStatus verificationStatus;
    private String documentReference;
    private String remarks;
}
