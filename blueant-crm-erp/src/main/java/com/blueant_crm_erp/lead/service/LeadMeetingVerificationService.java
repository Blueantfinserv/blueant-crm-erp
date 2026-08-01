package com.blueant_crm_erp.lead.service;

public interface LeadMeetingVerificationService {

    /**
     * Verifies the physical meeting for a Lead by a Process Coordinator.
     * Future modules (e.g. MeetingModule) will implement this.
     *
     * @param uniqueLeadId the unique lead ID
     * @param pcUserEmail the process coordinator's email
     */
    void verifyLeadMeeting(String uniqueLeadId, String pcUserEmail);
}
