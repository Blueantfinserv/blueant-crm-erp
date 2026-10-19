package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.FamilyStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilySummaryResponse {
    private Long id;
    private String familyCode;
    private String familyName;
    private Long familyHeadClientId;
    private String familyHeadClientCode;
    private String familyHeadClientName;
    private int activeMemberCount;
    private FamilyStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
}
