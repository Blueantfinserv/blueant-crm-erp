package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.InvestmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Sales-friendly workflow request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request schema for submitting a completed meeting update workflow.")
public class MeetingWorkflowRequest {

    @Schema(description = "Optional override for meeting date", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate meetingDate;

    @Schema(description = "Optional override for meeting time", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime meetingTime;

    @Schema(description = "Optional override for meeting mode (PHYSICAL, VIRTUAL/ONLINE)", allowableValues = {"PHYSICAL", "VIRTUAL/ONLINE"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MeetingMode meetingMode;

    @Schema(description = "Conducted status (Defaults to CONDUCTED. Rejects NOT_CONDUCTED)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MeetingConductStatus meetingConducted;

    @Size(max = 2000, message = "Discussion cannot exceed 2000 characters.")
    @Schema(description = "Discussion details", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String discussion;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    @Schema(description = "Optional meeting remarks", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String meetingRemarks;

    @Size(max = 50, message = "Completed stage cannot exceed 50 characters.")
    @Schema(description = "Stage completed during the meeting", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String completedStage;

    @Schema(description = "Lead status outcome (ALREADY_CLIENT, CONVERTED_CLIENT, CLIENT_REMOVED, CLIENT_NOT_INTERESTED, WORK_IN_PROGRESS)", requiredMode = Schema.RequiredMode.REQUIRED)
    private MeetingLeadStatus leadStatus;

    @Size(max = 50, message = "Client status cannot exceed 50 characters.")
    @Schema(description = "Client engagement status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String clientStatus;

    @Size(max = 255, message = "Joined meeting with cannot exceed 255 characters.")
    @Schema(description = "Who from company side joined", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String joinedMeetingWith;

    @Size(max = 20, message = "Alone with cannot exceed 20 characters.")
    @Schema(description = "Alone with status (SELF or SOMEONE)", allowableValues = {"SELF", "SOMEONE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String aloneWith;

    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    @Schema(description = "Person name if aloneWith is SOMEONE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String personName;

    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    @Schema(description = "Position if aloneWith is SOMEONE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String position;

    @Size(max = 100, message = "Leader name cannot exceed 100 characters.")
    @Schema(description = "Leader/Manager name who attended", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String leaderName;

    @Schema(description = "Next plan / follow-up date", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate nextPlanDate;

    @Size(max = 20, message = "PAN number cannot exceed 20 characters.")
    @Schema(description = "Client PAN number", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String panNumber;

    @Schema(description = "Estimated investment amount discussed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal investmentAmount;

    @Size(max = 100, message = "Product type cannot exceed 100 characters.")
    @Schema(description = "Product type discussed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String productType;

    @Schema(description = "Legacy reason field (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reason;

    @Schema(description = "Next plan time", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalTime nextPlanTime;

    @Schema(description = "Current investment company (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String currentInvestmentCompany;

    @Schema(description = "Current advisor (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String currentAdvisor;

    @Schema(description = "Investment type (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InvestmentType investmentType;

    @Schema(description = "Investment company (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String investmentCompany;

    @Schema(description = "Current stage (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String currentStage;

    @Schema(description = "GPS latitude", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private java.math.BigDecimal latitude;

    @Schema(description = "GPS longitude", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private java.math.BigDecimal longitude;

    @Schema(description = "GPS location address", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String address;

    @Schema(description = "GPS location accuracy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Double accuracy;
}

