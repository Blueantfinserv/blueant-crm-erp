package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ChangeFamilyHeadRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FamilyService {

    FamilyDetailResponse createFamily(CreateFamilyRequest request, String authenticatedUsername);

    Page<FamilySummaryResponse> getFamilies(Pageable pageable, String search, FamilyStatus status);

    FamilyDetailResponse getFamilyByCode(String familyCode);

    FamilyMemberResponse addMember(String familyCode, AddFamilyMemberRequest request, String authenticatedUsername);

    List<EligibleFamilyHeadResponse> getEligibleHeads(String familyCode);

    FamilyDetailResponse changeFamilyHead(String familyCode, ChangeFamilyHeadRequest request, String authenticatedUsername);

    List<FamilyHeadHistoryResponse> getFamilyHeadHistory(String familyCode);
}
