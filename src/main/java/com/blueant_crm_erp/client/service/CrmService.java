package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.CrmClientAssignRequest;
import com.blueant_crm_erp.client.dto.request.CrmOnboardingRequest;
import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.request.RecordClientFollowUpRequest;
import com.blueant_crm_erp.client.dto.response.*;

import java.util.List;

public interface CrmService {

    /**
     * Fetch all leads eligible for CRM.
     * Enforces:
     * 1. Must be PC-verified.
     * 2. Lead status must be CONVERTED_CLIENT.
     * 3. WIP leads are NEVER returned.
     */
    List<CrmLeadQueueResponse> getCrmQueue(String currentUserEmail);

    /**
     * Get CRM Onboarding data for a lead.
     */
    CrmOnboardingResponse getOnboarding(String leadCode, String currentUserEmail);

    /**
     * Create, save or submit CRM Onboarding Questions 1-28 for a CONVERTED_CLIENT lead.
     * Payment Done? = YES -> status PAYMENT_DONE (ready for CRM verification)
     * Payment Done? = NO  -> status DRAFT (remains draft, no client created)
     */
    CrmOnboardingResponse saveOrSubmitOnboarding(String leadCode, CrmOnboardingRequest request, String currentUserEmail);

    /**
     * Submit CRM Verification & verify the converted lead, creating the active Client.
     * Only permitted if Payment Done = YES.
     * Original Sales Person is permanently set as Created By.
     * Initial follow-up is scheduled for ~3 months.
     */
    CrmVerificationResponse verifyCrm(String leadCode, CrmVerificationRequest request, String currentUserEmail);

    /**
     * Assign / Reassign a Client to a Sales Person.
     * Updates assigned Sales Person while strictly preserving immutable Created By (original Sales Person).
     */
    ClientResponse assignClient(String clientCode, CrmClientAssignRequest request, String currentUserEmail);

    /**
     * Get ~3-month client follow-ups for Sales Person.
     */
    List<ClientFollowUpResponse> getClientFollowUps(String salesPersonIdentifier, String currentUserEmail);

    /**
     * Record a completed client follow-up and schedule the next ~3-month recurring follow-up.
     */
    ClientFollowUpRecordResponse recordClientFollowUp(String clientCode, RecordClientFollowUpRequest request, String currentUserEmail);
}
