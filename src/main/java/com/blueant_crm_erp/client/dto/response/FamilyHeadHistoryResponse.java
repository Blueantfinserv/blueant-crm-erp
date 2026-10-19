package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyHeadHistoryResponse {
    private Long id;
    private Long familyId;
    private String familyCode;
    private Long previousHeadClientId;
    private String previousHeadClientCode;
    private String previousHeadClientName;
    private Long newHeadClientId;
    private String newHeadClientCode;
    private String newHeadClientName;
    private String changedByUser;
    private LocalDateTime changedAt;
    private String reason;
}
