package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.client.enums.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientRequest {

    private ClientStatus clientStatus;

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String clientName;

    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 20, message = "PAN number must not exceed 20 characters")
    private String panNumber;

    @Size(max = 100, message = "AMC must not exceed 100 characters")
    private String amc;

    @Size(max = 150, message = "Scheme must not exceed 150 characters")
    private String scheme;

    @Size(max = 50, message = "Investment Type must not exceed 50 characters")
    private String investmentType;

    private LocalDate clientSince;
    private Long relationshipManagerId;
    private Long crmOwnerId;
}
