package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNomineeRequest {

    @Size(max = 150, message = "Nominee name cannot exceed 150 characters.")
    private String nomineeName;

    @Size(max = 50, message = "Relationship cannot exceed 50 characters.")
    private String relationship;

    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Contact number cannot exceed 20 characters.")
    private String contactNumber;

    @Size(max = 20, message = "PAN number cannot exceed 20 characters.")
    private String panNumber;

    @Builder.Default
    private Boolean isMinor = Boolean.FALSE;

    @Size(max = 150, message = "Guardian name cannot exceed 150 characters.")
    private String guardianName;

    @Size(max = 50, message = "Guardian relationship cannot exceed 50 characters.")
    private String guardianRelationship;

    @Size(max = 20, message = "Guardian contact cannot exceed 20 characters.")
    private String guardianContact;

    @Size(max = 20, message = "Guardian PAN cannot exceed 20 characters.")
    private String guardianPan;
}
