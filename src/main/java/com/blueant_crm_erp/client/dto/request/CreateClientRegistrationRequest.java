package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

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
}
