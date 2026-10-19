package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegistrationResponse {

    // Identity & Core (Existing)
    private Long id;
    private String clientCode;
    private String clientName;
    private String mobileNumber;
    private String alternateMobileNumber;
    private String email;
    private String speciality;
    private String location;
    private String clinicAddress;
    private String clientStatus;
    private String remarks;
    private LocalDate assignmentDate;
    private String bestTimeToMeet;

    // Assigned Sales Person (Existing)
    private Long assignedSalesPersonId;
    private String assignedSalesPersonCode;
    private String assignedSalesPersonName;
    private String assignedSalesPersonRole;

    // Assignment & Creator Metadata (Existing & Enhanced)
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private String createdByRole;

    private Long previousSalesPersonId;
    private String previousSalesPersonCode;
    private String previousSalesPersonName;

    // Section A: Personal & Family Details
    private String panNumber;
    private LocalDate dateOfBirth;
    private String motherName;
    private String placeOfBirth;
    private String occupation;
    private String familyHeadName;
    private String familyDetails;
    private String residentialAddress;
    private String correspondenceAddress;
    private String officeAddress;
    private String city;
    private String state;
    private String pinCode;

    // Section B: Client Source & Ownership
    private String clientSource;
    private String sourceDescription;

    // Section C: Investment & Application Details
    private LocalDate applicationReceivedDate;
    private String applicationMode;
    private String investmentType;
    private List<String> investmentTypes;
    private BigDecimal firstInvestmentAmount;
    private BigDecimal expectedMaxSIP;
    private String investwellId;
    private LocalDate clientReportDate;
    private ClientPaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private String paymentReference;

    // Section D: Nominees
    private List<ClientNomineeResponse> nominees;

    // Section E: Documents & Completion Status
    private List<ClientDocumentResponse> documents;
    private String documentCompletionStatus;

    // Section F: Family Management
    private Long familyId;
    private String familyCode;
    private String familyName;
    private Boolean isFamilyHead;
}
