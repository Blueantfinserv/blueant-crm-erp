package com.blueant_crm_erp.lead.service;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.lead.dto.request.*;
import com.blueant_crm_erp.lead.dto.response.*;
import org.springframework.data.domain.Pageable;

public interface LeadService {

    LeadResponse createLead(CreateLeadRequest request, String currentUserEmail);

    LeadResponse updateLead(String uniqueLeadId, UpdateLeadRequest request, String currentUserEmail);

    LeadResponse assignLead(AssignLeadRequest request, String currentUserEmail);

    LeadResponse transferLead(TransferLeadRequest request, String currentUserEmail);

    LeadResponse changeStatus(UpdateLeadStatusRequest request, String currentUserEmail);

    LeadResponse changePriority(ChangeLeadPriorityRequest request, String currentUserEmail);

    LeadResponse convertLead(ConvertLeadRequest request, String currentUserEmail);

    LeadDetailResponse getLeadDetails(String uniqueLeadId);

    PageResponse<LeadResponse> searchLeads(LeadSearchRequest request, Pageable pageable);

    PageResponse<LeadResponse> filterLeads(LeadFilterRequest request, Pageable pageable);

    void markDuplicate(String uniqueLeadId, String currentUserEmail);

    void verifyDuplicate(String uniqueLeadId, String currentUserEmail);

    void deleteLead(String uniqueLeadId, String currentUserEmail);

    void restoreLead(String uniqueLeadId, String currentUserEmail);

    void deactivateLead(String uniqueLeadId, String currentUserEmail);
}
