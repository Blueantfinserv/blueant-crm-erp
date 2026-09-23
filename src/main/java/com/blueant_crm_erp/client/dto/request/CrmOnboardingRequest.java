package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOnboardingRequest {

    // Question 1: Investor Name
    @Size(max = 150, message = "Investor name cannot exceed 150 characters.")
    private String investorName;

    // Question 2: Is he an investor with BLUEANT?
    private Boolean isBlueantInvestor;

    // Question 3: Family Head
    @Size(max = 150, message = "Family head cannot exceed 150 characters.")
    private String familyHead;

    // Question 4: Occupation
    @Size(max = 100, message = "Occupation cannot exceed 100 characters.")
    private String occupation;

    // Question 5: Created By (Original Sales Person is preserved automatically)
    private String createdBySalesPersonCode;

    // Question 6: PAN No
    @Size(max = 20, message = "PAN number cannot exceed 20 characters.")
    private String panNumber;

    // Question 7: Contact Detail
    @Size(max = 30, message = "Contact detail cannot exceed 30 characters.")
    private String contactDetail;

    // Question 8: Mail ID
    @Size(max = 150, message = "Mail ID cannot exceed 150 characters.")
    private String mailId;

    // Question 9: Correspondence Address
    @Size(max = 500, message = "Correspondence address cannot exceed 500 characters.")
    private String correspondenceAddress;

    // Question 10: Current / Office Address
    @Size(max = 500, message = "Office address cannot exceed 500 characters.")
    private String officeAddress;

    // Question 11: Place of Birth
    @Size(max = 100, message = "Place of birth cannot exceed 100 characters.")
    private String placeOfBirth;

    // Question 12: Family Details
    @Size(max = 1000, message = "Family details cannot exceed 1000 characters.")
    private String familyDetails;

    // Question 13: Location
    @Size(max = 150, message = "Location cannot exceed 150 characters.")
    private String location;

    // Question 14: Source
    @Size(max = 100, message = "Source cannot exceed 100 characters.")
    private String source;

    // Question 15: Description of Source
    @Size(max = 500, message = "Source description cannot exceed 500 characters.")
    private String sourceDescription;

    // Question 16: Application Received Date
    private LocalDate applicationReceivedDate;

    // Question 17: Nominee Details
    @Size(max = 500, message = "Nominee details cannot exceed 500 characters.")
    private String nomineeDetails;

    // Question 18: Nominee PAN No. / Aadhaar No.
    @Size(max = 50, message = "Nominee PAN/Aadhaar cannot exceed 50 characters.")
    private String nomineePanOrAadhaar;

    // Question 19: Mother's Name
    @Size(max = 150, message = "Mother's name cannot exceed 150 characters.")
    private String motherName;

    // Question 20: Application Submitted (ONLINE, OFFLINE)
    @Size(max = 50, message = "Application mode cannot exceed 50 characters.")
    private String applicationMode;

    // Question 21: Amount of 1st Investment
    private BigDecimal firstInvestmentAmount;

    // Question 22: How much Max SIP can you expect from him?
    private BigDecimal expectedMaxSIP;

    // Question 23: Type of Investment
    @Size(max = 100, message = "Investment type cannot exceed 100 characters.")
    private String investmentType;

    // Question 24: All Document Completed (YES, NO)
    private Boolean allDocumentsCompleted;

    // Question 25: Investwell User ID
    @Size(max = 100, message = "Investwell User ID cannot exceed 100 characters.")
    private String investwellUserId;

    // Question 26: Helpdesk Query No.
    @Size(max = 100, message = "Helpdesk query number cannot exceed 100 characters.")
    private String helpdeskQueryNo;

    // Question 27: Client Reported Date
    private LocalDate clientReportedDate;

    // Question 28: Payment Done? (YES, NO)
    private Boolean paymentDone;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;
}
