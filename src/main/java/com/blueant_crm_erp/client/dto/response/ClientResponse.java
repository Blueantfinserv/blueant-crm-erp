package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private Long leadId;
    private String clientCode;
    private ClientStatus clientStatus;
    private String clientName;
    private String mobileNumber;
    private String email;
    private String panNumber;
    private String amc;
    private String scheme;
    private String investmentType;
    private LocalDate clientSince;
    private Long relationshipManagerId;
    private Long crmOwnerId;
}
