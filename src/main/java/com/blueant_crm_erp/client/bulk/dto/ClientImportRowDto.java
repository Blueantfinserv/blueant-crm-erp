package com.blueant_crm_erp.client.bulk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientImportRowDto {

    private int rowNumber;

    // Six mandatory fields
    private String clientName;
    private String mobileNumber;
    private String speciality;
    private String location;
    private String clinicAddress;
    private String salesPersonEmployeeCode;

    // Optional profile fields
    private String alternateMobileNumber;
    private String email;
    private String remarks;
    private String bestTimeToMeet;
    private String panNumber;
    private LocalDate dateOfBirth;
    private LocalDate actualDateOfBirth;
    private String motherName;
    private String placeOfBirth;
    private String occupation;
    private String residentialAddress;
    private String correspondenceAddress;
    private String officeAddress;
    private String city;
    private String state;
    private String pinCode;
    private String clientSource;
    private String sourceDescription;
    private BigDecimal expectedMaxSIP;
    private String investwellId;
    private String investmentType;
    private BigDecimal firstInvestmentAmount;
    private LocalDate assignmentDate;
}

