package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDocumentRequest {

    private ClientDocumentType documentType;

    @Size(max = 150, message = "Document name cannot exceed 150 characters.")
    private String documentName;

    @Builder.Default
    private Boolean received = Boolean.FALSE;

    private LocalDate receivedDate;

    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Size(max = 255, message = "Document reference cannot exceed 255 characters.")
    private String documentReference;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters.")
    private String remarks;
}
