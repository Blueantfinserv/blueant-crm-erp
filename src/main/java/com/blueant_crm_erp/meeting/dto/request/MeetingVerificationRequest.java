package com.blueant_crm_erp.meeting.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVerificationRequest {

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    @NotBlank(message = "Who was present status is required.")
    @Pattern(regexp = "^(SELF|SOMEONE)$", message = "aloneWith must be SELF or SOMEONE.")
    private String aloneWith;

    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    private String personName;

    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    private String position;

    @NotNull(message = "Client age is required.")
    @Min(value = 18, message = "Client age must be at least 18.")
    @Max(value = 120, message = "Client age must be at most 120.")
    private Integer clientAge;

    @NotBlank(message = "Marital status is required.")
    private String maritalStatus;

    @NotBlank(message = "Profession is required.")
    private String profession;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Company name is required.")
    private String companyName;

    @NotNull(message = "Children status is required.")
    private Boolean anyChildren;

    private Integer numberOfChildren;

    @NotNull(message = "Previous investment status is required.")
    private Boolean previousInvestment;

}
