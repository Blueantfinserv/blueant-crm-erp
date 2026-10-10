package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.response.ClientRegistrationResponse;

public interface ClientRegistrationService {
    ClientRegistrationResponse registerClient(CreateClientRegistrationRequest request, String currentUserIdentifier);
}
