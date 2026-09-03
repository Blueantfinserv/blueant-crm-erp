package com.blueant_crm_erp.lead.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.lead.dto.request.AssignPhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreatePhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.dto.response.PhysicalLeadAssignmentResponse;
import org.springframework.data.domain.Pageable;

public interface PhysicalLeadService {

    /**
     * Creates a new physical lead and optionally assigns it to a Sales Person.
     */
    PhysicalLeadAssignmentResponse createPhysicalLead(CreatePhysicalLeadRequest request, String currentUserIdentifier);

    /**
     * Assigns an existing physical lead to a Sales Person.
     */
    PhysicalLeadAssignmentResponse assignPhysicalLead(String leadCode, AssignPhysicalLeadRequest request, String currentUserIdentifier);

    /**
     * Returns eligible unassigned physical leads for Sales Coordinator.
     */
    PageResponse<LeadResponse> getEligiblePhysicalLeads(Pageable pageable);
}
