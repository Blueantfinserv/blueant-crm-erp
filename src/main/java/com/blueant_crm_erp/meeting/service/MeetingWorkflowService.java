package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;

/**
 * ============================================================================
 * Meeting Workflow Service (Orchestrator)
 * ============================================================================
 *
 * Orchestrates the end-to-end meeting update workflow.
 *
 * Responsibilities:
 * 1. Validate the workflow request.
 * 2. Delegate update persistence to MeetingUpdateService.
 * 3. Apply lead status transitions based on MeetingConductStatus and MeetingLeadStatus.
 * 4. Delegate follow-up creation to FollowUpService.
 * 5. Delegate lead status transitions to LeadService.
 * 6. Publish typed domain events.
 *
 * Business decisions are expressed as an explicit switch over MeetingLeadStatus,
 * with no external strategy or decision engine dependency.
 */
public interface MeetingWorkflowService {

    /**
     * Processes a meeting update workflow submission from a sales rep.
     *
     * @param meetingCode      the meeting being updated
     * @param request          the workflow request with outcome and fields
     * @param currentUserEmail the authenticated user email
     * @return the response of the resulting meeting (current or new follow-up)
     */
    MeetingResponse processWorkflow(String meetingCode, MeetingWorkflowRequest request, String currentUserEmail);
}
