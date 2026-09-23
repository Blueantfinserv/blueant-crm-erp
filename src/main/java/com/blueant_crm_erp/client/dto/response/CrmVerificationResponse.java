package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmVerificationResponse {

    private Long id;
    private Long leadId;
    private String leadCode;
    private Long clientId;
    private String clientCode;
    private String clientName;
    private String verificationStatus;

    // Questions / Checklist
    private Boolean kycVerified;
    private Boolean bankDetailsVerified;
    private Boolean documentsVerified;
    private Boolean clientContactConfirmed;
    private String panNumber;
    private String remarks;

    // Audit
    private String verifiedBy;
    private LocalDateTime verifiedAt;

    // Sales Person Continuity
    private String salesPersonCode;
    private String salesPersonName;
    private String createdBySalesPersonCode;
    private String createdBySalesPersonName;

    // Scheduled Follow-Up (~3 Months)
    private LocalDate nextFollowupDate;
}
