package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOnboardingResponse {

    private Long id;
    private Long leadId;
    private String leadCode;
    private String onboardingStatus;

    // Question 5: Created By (Original Sales Person)
    private Long createdBySalesPersonId;
    private String createdBySalesPersonCode;
    private String createdBySalesPersonName;

    // Questions 1 to 28
    private String investorName;
    private Boolean isBlueantInvestor;
    private String familyHead;
    private String occupation;
    private String panNumber;
    private String contactDetail;
    private String mailId;
    private String correspondenceAddress;
    private String officeAddress;
    private String placeOfBirth;
    private String familyDetails;
    private String location;
    private String source;
    private String sourceDescription;
    private LocalDate applicationReceivedDate;
    private String nomineeDetails;
    private String nomineePanOrAadhaar;
    private String motherName;
    private String applicationMode;
    private BigDecimal firstInvestmentAmount;
    private BigDecimal expectedMaxSIP;
    private String investmentType;
    private Boolean allDocumentsCompleted;
    private String investwellUserId;
    private String helpdeskQueryNo;
    private LocalDate clientReportedDate;
    private Boolean paymentDone;

    private String remarks;
    private LocalDateTime submittedAt;
    private String submittedBy;
}
