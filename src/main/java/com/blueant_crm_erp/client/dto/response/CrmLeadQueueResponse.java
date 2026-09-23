package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmLeadQueueResponse {

    private Long leadId;
    private String leadCode;
    private String uniqueLeadId;
    private String clientName;
    private String mobileNumber;
    private String email;
    private String location;

    // Sales Person Association (Preserved)
    private Long salesPersonId;
    private String salesPersonCode;
    private String salesPersonName;

    // PC Verification details
    private String verifiedMeetingCode;
    private LocalDateTime pcVerifiedAt;
    private String pcVerifiedBy;

    // Statuses
    private String leadStatus;
    private String leadStage;
    private String crmOnboardingStatus;
    private String crmVerificationStatus;
}
