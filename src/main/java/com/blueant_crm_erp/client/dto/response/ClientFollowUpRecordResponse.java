package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFollowUpRecordResponse {

    private Long id;
    private Long clientId;
    private String clientCode;
    private String clientName;

    private Long salesPersonId;
    private String salesPersonCode;
    private String salesPersonName;

    private LocalDate followupDate;
    private String remarks;
    private LocalDate nextFollowupDate;

    private LocalDateTime createdAt;
    private String createdBy;
}
