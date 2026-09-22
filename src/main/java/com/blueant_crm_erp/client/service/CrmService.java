package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientFollowUpResponse;
import com.blueant_crm_erp.client.dto.response.CrmLeadQueueResponse;
import com.blueant_crm_erp.client.dto.response.CrmVerificationResponse;

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
     * Submit CRM Questions & verify the converted lead, creating the active Client.
     * Client is assigned to the original Sales Person with ~3-month follow-up scheduled.
     */
    CrmVerificationResponse verifyCrm(String leadCode, CrmVerificationRequest request, String currentUserEmail);

    /**
     * Get ~3-month client follow-ups for Sales Person.
     */
    List<ClientFollowUpResponse> getClientFollowUps(String salesPersonIdentifier, String currentUserEmail);
}
