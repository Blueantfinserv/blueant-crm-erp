package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.dto.response.ClientResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.exception.ClientNotFoundException;
import com.blueant_crm_erp.client.mapper.ClientMapper;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.specification.ClientSpecification;
import com.blueant_crm_erp.client.validator.ClientValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Controller", description = "Endpoints for managing converted clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ClientValidator clientValidator;

    @Operation(summary = "Get Client by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('client:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + id));
    }

    @Operation(summary = "Search Clients")
    @GetMapping
    @PreAuthorize("hasAuthority('client:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @RequestParam(required = false) String clientCode,
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(required = false) Long rmId,
            Pageable pageable) {
        
        Specification<Client> spec = Specification.where(ClientSpecification.hasClientCode(clientCode))
                .and(ClientSpecification.hasStatus(status))
                .and(ClientSpecification.hasRelationshipManager(rmId));

        return ResponseEntity.ok(clientRepository.findAll(spec, pageable).map(clientMapper::toResponse));
    }

    @Operation(summary = "Update Client")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody UpdateClientRequest request) {
        clientValidator.validateUpdateRequest(request);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + id));
                
        clientMapper.updateEntityFromRequest(request, client);
        Client updated = clientRepository.save(client);
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }
}
