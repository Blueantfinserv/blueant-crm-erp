package com.blueant_crm_erp.lead.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Physical Lead Assignment Response
 * ============================================================================
 *
 * Description:
 * Standard response DTO returned after assigning a physical lead to a Sales Person.
 *
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalLeadAssignmentResponse {

    private Long leadId;
    private String leadCode;
    private String uniqueLeadId;
    private String clientName;
    private String mobileNumber;
    private String speciality;
    private String location;
    private String clinicAddress;
    private Boolean isPhysicalLead;

    // Assigned Sales Person details
    private Long assignedUserId;
    private String assignedEmployeeCode;
    private String assignedEmployeeName;

    // Assignment Audit details
    private String assignedByEmployeeCode;
    private String assignedByEmployeeName;
    private LocalDateTime assignedAt;
    private String assignmentSource;
    private Boolean assignedByCoordinator;
    private String assignmentLabel;

    private String statusMessage;
}
