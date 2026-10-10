package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegistrationResponse {

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

    private Long assignedSalesPersonId;
    private String assignedSalesPersonCode;
    private String assignedSalesPersonName;
    private String assignedSalesPersonRole;

    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private String createdBy;
}
