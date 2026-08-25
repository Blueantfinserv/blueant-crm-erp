package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Profession {

    SALARIED_EMPLOYEE("Salaried Employee"),
    BUSINESS_OWNER("Business Owner"),
    SELF_EMPLOYED("Self Employed"),
    DOCTOR("Doctor"),
    LAWYER_ADVOCATE("Lawyer / Advocate"),
    CHARTERED_ACCOUNTANT("Chartered Accountant (CA)"),
    COMPANY_SECRETARY("Company Secretary (CS)"),
    ENGINEER("Engineer"),
    ARCHITECT("Architect"),
    CONSULTANT("Consultant"),
    TEACHER_PROFESSOR("Teacher / Professor"),
    GOVERNMENT_EMPLOYEE("Government Employee"),
    BANKING_FINANCE_PROFESSIONAL("Banking / Finance Professional"),
    IT_SOFTWARE_PROFESSIONAL("IT / Software Professional"),
    HEALTHCARE_PROFESSIONAL("Healthcare Professional"),
    SALES_MARKETING_PROFESSIONAL("Sales / Marketing Professional"),
    REAL_ESTATE_PROFESSIONAL("Real Estate Professional"),
    TRADER_INVESTOR("Trader / Investor"),
    RETIRED("Retired"),
    STUDENT("Student"),
    HOMEMAKER("Homemaker"),
    OTHER("Other"),
    NOT_DISCLOSED("Not Disclosed");

    private final String displayName;

}
