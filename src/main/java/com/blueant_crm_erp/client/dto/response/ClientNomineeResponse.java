package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNomineeResponse {

    private Long id;
    private String nomineeName;
    private String relationship;
    private LocalDate dateOfBirth;
    private String contactNumber;
    private String panNumber;
    private Boolean isMinor;
    private String guardianName;
    private String guardianRelationship;
    private String guardianContact;
    private String guardianPan;
}
