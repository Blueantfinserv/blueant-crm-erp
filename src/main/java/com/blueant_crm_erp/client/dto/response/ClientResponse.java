package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Long salesPersonId;
    private String salesPersonCode;
    private String salesPersonName;
    private Long createdBySalesPersonId;
    private String createdBySalesPersonCode;
    private String createdBySalesPersonName;
    private LocalDate nextFollowupDate;

    // --- Extended Profile Fields ---
    private String speciality;
    private String location;
    private String clinicAddress;
    private String alternateMobileNumber;
    private LocalDate dateOfBirth;
    private LocalDate actualDateOfBirth;
    private String occupation;
    private String motherName;
    private String placeOfBirth;
    private String residentialAddress;
    private String correspondenceAddress;
    private String officeAddress;
    private String city;
    private String state;
    private String pinCode;
    private String clientSource;
    private String sourceDescription;
    private BigDecimal expectedMaxSIP;
    private BigDecimal firstInvestmentAmount;
    private String investwellId;
    private String bestTimeToMeet;
    private LocalDate assignmentDate;
    private String remarks;
}
