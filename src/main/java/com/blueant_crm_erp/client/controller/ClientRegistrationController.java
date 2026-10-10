package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.response.ClientRegistrationResponse;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Registration API", description = "Endpoints for external client registration and RM/SC assignment")
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;

    @Operation(summary = "Register new external client", description = "Registers an external client from Excel/external system and assigns them to an eligible active RM or SC")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientRegistrationResponse> registerClient(
            @Valid @RequestBody CreateClientRegistrationRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientRegistrationResponse response = clientRegistrationService.registerClient(request, currentUser);
        return ApiResponse.success("Client registered and assigned successfully", response);
    }
}
