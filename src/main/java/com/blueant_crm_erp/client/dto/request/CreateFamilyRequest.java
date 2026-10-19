package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.FamilyRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyRequest {

    @NotBlank(message = "Family name is required.")
    @Size(max = 150, message = "Family name cannot exceed 150 characters.")
    private String familyName;

    private Long initialHeadClientId;

    private FamilyRelationship initialHeadRelationship;
}
