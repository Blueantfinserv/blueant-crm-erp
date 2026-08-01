package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.event.ClientCreatedEvent;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientWorkflowService;
import com.blueant_crm_erp.lead.entity.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientWorkflowServiceImpl implements ClientWorkflowService {

    private final ClientRepository clientRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Client createClientFromLead(Lead lead, String panNumber, String currentUserEmail) {
        log.info("Creating client from lead: {}, by: {}", lead.getLeadCode(), currentUserEmail);

        if (clientRepository.findByLeadId(lead.getId()).isPresent()) {
            log.warn("Client already exists for lead: {}", lead.getLeadCode());
            return clientRepository.findByLeadId(lead.getId()).get();
        }

        Client client = Client.builder()
                .lead(lead)
                .clientCode("CLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .clientStatus(ClientStatus.ACTIVE)
                .clientName(lead.getClientName())
                .mobileNumber(lead.getMobileNumber())
                .email(lead.getEmail())
                .panNumber(panNumber)
                .build();

        Client savedClient = clientRepository.save(client);

        eventPublisher.publishEvent(new ClientCreatedEvent(this, savedClient, "Client onboarded automatically.", currentUserEmail));

        return savedClient;
    }
}
