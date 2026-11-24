package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveFamilyMemberRequest {

    @Size(max = 500, message = "Reason cannot exceed 500 characters.")
    private String reason;
}
