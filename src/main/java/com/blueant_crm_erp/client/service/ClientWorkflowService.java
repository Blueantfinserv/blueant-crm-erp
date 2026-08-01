package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.lead.entity.Lead;

public interface ClientWorkflowService {
    Client createClientFromLead(Lead lead, String panNumber, String currentUserEmail);
}
