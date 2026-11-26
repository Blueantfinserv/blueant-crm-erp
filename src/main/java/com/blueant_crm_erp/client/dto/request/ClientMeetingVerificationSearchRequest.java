package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * =============================================================================
 * Client Meeting Verification Search Request
 * =============================================================================
 *
 * Encapsulates search and filter criteria for Process Coordinator (PC)
 * Client Meeting Verification queues and audit screens.
 *
 * Supports role-wise filtering (RM, SC), client details, meeting code,
 * date ranges, verification status, business generated status, and the
 * 23 PC verification business options.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request criteria for searching and filtering client meeting verifications")
public class ClientMeetingVerificationSearchRequest {

    /**
     * General keyword filter searching across:
     * - Meeting Code
     * - Client Name
     * - Client Mobile Number
     * - Employee Name
     * - Employee Code
     */
    @Size(max = 100, message = "Keyword cannot exceed 100 characters.")
    @Schema(description = "Keyword to match meeting code, client name, mobile, employee name, or employee code", example = "Sharma")
    private String keyword;

    /**
     * Filter by assigned employee role: RM or SC.
     * Note: SM cannot have client meetings by business rules.
     */
    @Schema(description = "Filter by assigned employee role (RM, SC)", example = "RM")
    private SalesRole employeeRole;

    @Size(max = 100, message = "Employee name cannot exceed 100 characters.")
    @Schema(description = "Filter by assigned employee first or last name", example = "Vikram")
    private String employeeName;

    @Size(max = 50, message = "Employee code cannot exceed 50 characters.")
    @Schema(description = "Filter by assigned employee code", example = "EMP001")
    private String employeeCode;

    @Size(max = 50, message = "Meeting code cannot exceed 50 characters.")
    @Schema(description = "Filter by meeting business code", example = "BA-CLM-202611-0001")
    private String meetingCode;

    @Size(max = 150, message = "Client name cannot exceed 150 characters.")
    @Schema(description = "Filter by client name", example = "Rajesh Sharma")
    private String clientName;

    @Size(max = 20, message = "Client mobile number cannot exceed 20 characters.")
    @Schema(description = "Filter by client mobile number", example = "9876543210")
    private String mobileNumber;

    /**
     * Date Filters applied to ClientMeeting.meetingDate (inclusive)
     */
    @Schema(description = "Inclusive start date filter (meeting date >= fromDate)", example = "2026-11-01")
    private LocalDate fromDate;

    @Schema(description = "Inclusive end date filter (meeting date <= toDate)", example = "2026-11-30")
    private LocalDate toDate;

    /**
     * Verification Status Filter: PENDING, VERIFIED, REJECTED
     */
    @Schema(description = "Filter by verification status (PENDING, VERIFIED, REJECTED)", example = "VERIFIED")
    private VerificationStatus verificationStatus;

    /**
     * Business Generated Filter: YES or NO
     */
    @Schema(description = "Filter by business generated status (YES, NO)", example = "YES")
    private String businessGenerated;

    /**
     * Business Type Filter: One of the 23 supported PC Verification business options
     */
    @Schema(description = "Filter by business option (e.g. 'SIP STP Start', 'Lumpsum Switch / Change')", example = "SIP STP Start")
    private String businessType;
}
