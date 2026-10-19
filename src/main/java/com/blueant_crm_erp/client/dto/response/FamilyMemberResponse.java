package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberResponse {
    private Long memberId;
    private Long clientId;
    private String clientCode;
    private String clientName;
    private String mobileNumber;
    private FamilyRelationship relationshipToHead;
    private FamilyMembershipStatus membershipStatus;
    @com.fasterxml.jackson.annotation.JsonProperty("isFamilyHead")
    private Boolean isFamilyHead;
    private LocalDateTime joinedAt;
    private String joinedBy;
}
