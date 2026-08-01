package com.blueant_crm_erp.meeting.service;

import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingUpdateResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;

import java.util.List;

/**
 * ============================================================================
 * Meeting Update Service
 * ============================================================================
 *
 * Responsible for persisting immutable MeetingUpdate audit records
 * and applying denormalized field updates to the parent Meeting entity.
 *
 * Single Responsibility: Data persistence only.
 * Does NOT evaluate outcomes or route workflow decisions.
 */
public interface MeetingUpdateService {

    /**
     * Creates an immutable MeetingUpdate record and applies denormalized
     * updates to the parent Meeting entity.
     *
     * @param meeting     the parent meeting entity
     * @param request     the workflow request from sales
     * @param submittedBy the authenticated user email
     * @return the persisted MeetingUpdate record
     */
    MeetingUpdate persistUpdate(Meeting meeting, MeetingWorkflowRequest request, String submittedBy);

    /**
     * Returns the complete update history for a meeting, ordered by update number ascending.
     *
     * @param meetingCode the meeting business code
     * @return immutable list of update records
     */
    List<MeetingUpdateResponse> getUpdateHistory(String meetingCode);
}
