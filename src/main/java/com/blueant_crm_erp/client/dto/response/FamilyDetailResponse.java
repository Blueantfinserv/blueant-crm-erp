package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.FamilyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDetailResponse {
    private Long id;
    private String familyCode;
    private String familyName;
    private Long familyHeadClientId;
    private String familyHeadClientCode;
    private String familyHeadClientName;
    private FamilyStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<FamilyMemberResponse> members;
}
