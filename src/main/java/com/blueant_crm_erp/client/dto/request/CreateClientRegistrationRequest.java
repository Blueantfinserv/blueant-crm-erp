package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientRegistrationRequest {

    @NotBlank(message = "Client name is required.")
    @Size(max = 150, message = "Client name cannot exceed 150 characters.")
    private String clientName;

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be a valid 10-digit number.")
    private String mobileNumber;

    @NotBlank(message = "Speciality is required.")
    @Size(max = 100, message = "Speciality cannot exceed 100 characters.")
    private String speciality;

    @NotBlank(message = "Location is required.")
    @Size(max = 150, message = "Location cannot exceed 150 characters.")
    private String location;

    @NotBlank(message = "Clinic address is required.")
    @Size(max = 500, message = "Clinic address cannot exceed 500 characters.")
    private String clinicAddress;

    @NotBlank(message = "Sales person employee code is required.")
    private String salesPersonEmployeeCode;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Alternate mobile number must be a valid 10-digit number.")
    private String alternateMobileNumber;

    @Email(message = "Invalid email format.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    private String email;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    private LocalDate assignmentDate;

    private String bestTimeToMeet;

    // --- Section A: Personal & Family Details (Optional) ---

    @Pattern(regexp = "^$|^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format. Expected format: ABCDE1234F.")
    private String panNumber;

    @Past(message = "Date of birth must be in the past.")
    private LocalDate dateOfBirth;

    @Past(message = "Actual date of birth must be in the past.")
    private LocalDate actualDateOfBirth;

    @Size(max = 150, message = "Mother's name cannot exceed 150 characters.")
    private String motherName;

    @Size(max = 100, message = "Place of birth cannot exceed 100 characters.")
    private String placeOfBirth;

    @Size(max = 100, message = "Occupation cannot exceed 100 characters.")
    private String occupation;

    @Size(max = 150, message = "Family head name cannot exceed 150 characters.")
    private String familyHeadName;

    @Size(max = 1000, message = "Family details cannot exceed 1000 characters.")
    private String familyDetails;

    @Size(max = 500, message = "Residential address cannot exceed 500 characters.")
    private String residentialAddress;

    @Size(max = 500, message = "Correspondence address cannot exceed 500 characters.")
    private String correspondenceAddress;

    @Size(max = 500, message = "Office address cannot exceed 500 characters.")
    private String officeAddress;

    @Size(max = 100, message = "City cannot exceed 100 characters.")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters.")
    private String state;

    @Size(max = 20, message = "PIN code cannot exceed 20 characters.")
    private String pinCode;

    // --- Section B: Client Source & Ownership (Optional) ---

    @Size(max = 100, message = "Client source cannot exceed 100 characters.")
    private String clientSource;

    @Size(max = 500, message = "Source description cannot exceed 500 characters.")
    private String sourceDescription;

    // --- Section C: Investment & Application Details (Optional) ---

    private LocalDate applicationReceivedDate;

    @Size(max = 50, message = "Application mode cannot exceed 50 characters.")
    private String applicationMode;

    @Size(max = 255, message = "Investment type cannot exceed 255 characters.")
    private String investmentType;

    private List<String> investmentTypes;

    @DecimalMin(value = "0.0", inclusive = true, message = "First investment amount must be non-negative.")
    private BigDecimal firstInvestmentAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Expected max SIP must be non-negative.")
    private BigDecimal expectedMaxSIP;

    @Size(max = 100, message = "Investwell ID cannot exceed 100 characters.")
    private String investwellId;

    private LocalDate clientReportDate;

    private ClientPaymentStatus paymentStatus;

    @DecimalMin(value = "0.0", inclusive = true, message = "Payment amount must be non-negative.")
    private BigDecimal paymentAmount;

    private LocalDate paymentDate;

    @Size(max = 100, message = "Payment reference cannot exceed 100 characters.")
    private String paymentReference;

    // --- Section D: Nominees (Optional) ---

    @Valid
    private List<ClientNomineeRequest> nominees;

    // --- Section E: Document Checklist (Optional) ---

    @Valid
    private List<ClientDocumentRequest> documents;

    // --- Section F: Family Management (Optional) ---

    @Size(max = 50, message = "Family code cannot exceed 50 characters.")
    private String familyCode;

    @Size(max = 150, message = "New family name cannot exceed 150 characters.")
    private String newFamilyName;

    private com.blueant_crm_erp.client.enums.FamilyRelationship relationshipToHead;

    private Boolean designateAsHead;
}
