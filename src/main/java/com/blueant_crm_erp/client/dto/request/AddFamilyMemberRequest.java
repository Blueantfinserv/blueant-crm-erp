package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.FamilyRelationship;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFamilyMemberRequest {

    @NotNull(message = "Client ID is required.")
    private Long clientId;

    private FamilyRelationship relationshipToHead;

    private Boolean designateAsHead;
}
