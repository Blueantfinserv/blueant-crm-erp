package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.ClientStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class UpdateClientRequest {

    private ClientStatus clientStatus;

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String clientName;

    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 20, message = "PAN number must not exceed 20 characters")
    private String panNumber;

    @Size(max = 100, message = "AMC must not exceed 100 characters")
    private String amc;

    @Size(max = 150, message = "Scheme must not exceed 150 characters")
    private String scheme;

    @Size(max = 255, message = "Investment Type must not exceed 255 characters")
    private String investmentType;

    private LocalDate clientSince;
    private Long relationshipManagerId;
    private Long crmOwnerId;

    // --- Extended Profile Fields (Post-Import Editability) ---

    @Size(max = 100, message = "Speciality must not exceed 100 characters")
    private String speciality;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Clinic address must not exceed 500 characters")
    private String clinicAddress;

    @Size(max = 20, message = "Alternate mobile number must not exceed 20 characters")
    private String alternateMobileNumber;

    private LocalDate dateOfBirth;
    private LocalDate actualDateOfBirth;

    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    @Size(max = 150, message = "Mother name must not exceed 150 characters")
    private String motherName;

    @Size(max = 100, message = "Place of birth must not exceed 100 characters")
    private String placeOfBirth;

    @Size(max = 500, message = "Residential address must not exceed 500 characters")
    private String residentialAddress;

    @Size(max = 500, message = "Correspondence address must not exceed 500 characters")
    private String correspondenceAddress;

    @Size(max = 500, message = "Office address must not exceed 500 characters")
    private String officeAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "PIN code must not exceed 20 characters")
    private String pinCode;

    @Size(max = 100, message = "Client source must not exceed 100 characters")
    private String clientSource;

    @Size(max = 500, message = "Source description must not exceed 500 characters")
    private String sourceDescription;

    @DecimalMin(value = "0.0", inclusive = true, message = "Expected max SIP must be non-negative")
    private BigDecimal expectedMaxSIP;

    @DecimalMin(value = "0.0", inclusive = true, message = "First investment amount must be non-negative")
    private BigDecimal firstInvestmentAmount;

    @Size(max = 100, message = "Investwell ID must not exceed 100 characters")
    private String investwellId;

    @Size(max = 50, message = "Best time to meet must not exceed 50 characters")
    private String bestTimeToMeet;

    private LocalDate assignmentDate;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;
}
