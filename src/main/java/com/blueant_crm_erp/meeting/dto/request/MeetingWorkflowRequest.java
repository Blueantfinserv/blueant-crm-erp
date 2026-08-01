package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
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
 *
 * Lead details are NOT accepted here — they are fetched internally by the backend.
 * Only the fields a sales representative needs to complete a meeting update.
 *
 * All new fields are optional for backward compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingWorkflowRequest {

    // ─── Current Meeting Updates ───────────────────────────────────────────

    /** Optional: override meeting date (e.g. meeting ran on a different day) */
    private LocalDate meetingDate;

    /** Optional: override meeting time */
    private LocalTime meetingTime;

    /** Optional: override meeting mode — PHYSICAL, ONLINE, PHONE */
    private MeetingMode meetingMode;

    /** Whether the meeting was physically conducted */
    private Boolean meetingConducted;

    /** What was discussed in the meeting (max 2000 chars) */
    @Size(max = 2000, message = "Discussion cannot exceed 2000 characters.")
    private String discussion;

    /** Outcome of this meeting */
    private MeetingOutcome meetingOutcome;

    /** Any additional remarks (max 1000 chars) */
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String meetingRemarks;

    // ─── Sales Stage Fields ────────────────────────────────────────────────

    /** Stage completed in this meeting (e.g. INTRO_MEETING, PRODUCT_DISCUSSION) */
    @Size(max = 50, message = "Completed stage cannot exceed 50 characters.")
    private String completedStage;

    /** Current lead status as observed by sales rep */
    @Size(max = 50, message = "Lead status cannot exceed 50 characters.")
    private String leadStatus;

    /** Client engagement status (e.g. Interested, Thinking, Not Interested) */
    @Size(max = 50, message = "Client status cannot exceed 50 characters.")
    private String clientStatus;

    /** Who joined the meeting from the company side */
    @Size(max = 255, message = "Joined meeting with cannot exceed 255 characters.")
    private String joinedMeetingWith;

    /** Alone with (SELF or SOMEONE) */
    @Size(max = 20, message = "Alone with cannot exceed 20 characters.")
    private String aloneWith;

    /** Person Name if SOMEONE */
    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    private String personName;

    /** Position if SOMEONE */
    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    private String position;

    /** Leader/Manager name who attended */
    @Size(max = 100, message = "Leader name cannot exceed 100 characters.")
    private String leaderName;

    /** Next plan / follow-up date */
    private LocalDate nextPlanDate;

    // ─── Investment Fields ─────────────────────────────────────────────────

    /** Client PAN number (optional, captured for KYC) */
    @Size(max = 20, message = "PAN number cannot exceed 20 characters.")
    private String panNumber;

    /** Estimated investment amount discussed */
    private BigDecimal investmentAmount;

    /** Product type discussed (e.g. Mutual Fund, SIP, Lump Sum) */
    @Size(max = 100, message = "Product type cannot exceed 100 characters.")
    private String productType;

    // ─── Next Follow-up Scheduling ─────────────────────────────────────────

    /**
     * Set to true to schedule a follow-up meeting.
     * When true, nextMeetingDate and nextMeetingTime become required.
     * The follow-up mode inherits from the current meeting.
     */
    private Boolean scheduleNextMeeting;

    /** Required when outcome is FOLLOW_UP_REQUIRED */
    private LocalDate nextMeetingDate;

    /** Required when outcome is FOLLOW_UP_REQUIRED */
    private LocalTime nextMeetingTime;
}

