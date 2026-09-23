package com.blueant_crm_erp.client.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordClientFollowUpRequest {

    private LocalDate followupDate;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    private LocalDate nextFollowupDate;
}
