package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFollowUpResponse {

    private Long clientId;
    private String clientCode;
    private String clientName;
    private String mobileNumber;
    private String email;

    private Long salesPersonId;
    private String salesPersonCode;
    private String salesPersonName;

    private LocalDate clientSince;
    private LocalDate nextFollowupDate;
    private Long daysUntilFollowUp;
}
