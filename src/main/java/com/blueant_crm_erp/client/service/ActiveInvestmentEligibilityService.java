package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.entity.Client;

public interface ActiveInvestmentEligibilityService {

    /**
     * Checks whether a client is eligible to be designated as Family Head
     * based on authoritative active investment data.
     */
    boolean isEligibleForFamilyHead(Client client);

    /**
     * Evaluates eligibility and returns details on whether and why the client qualifies or fails.
     */
    EligibilityResult evaluateEligibility(Client client);

    record EligibilityResult(boolean eligible, String reason, String qualifyingDetails) {}
}
