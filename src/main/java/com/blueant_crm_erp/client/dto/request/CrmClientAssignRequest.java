package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClientAssignRequest {

    private Long salesPersonId;

    private String salesPersonCode;

    private String salesPersonIdentifier;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;
}
