package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientCodeGeneratorService;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.util.id.ClientCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientCodeGeneratorServiceImpl implements ClientCodeGeneratorService {

    private final LeadCodeSequenceRepository sequenceRepository;
    private final ClientRepository clientRepository;

    public static final String SEQUENCE_NAME = "CLIENT_CODE";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextClientCode() {
        LeadCodeSequence sequence = sequenceRepository.findBySequenceNameWithLock(SEQUENCE_NAME)
                .orElseGet(() -> {
                    LeadCodeSequence newSeq = LeadCodeSequence.builder()
                            .sequenceName(SEQUENCE_NAME)
                            .currentValue(0L)
                            .build();
                    log.info("Initialized sequence {} with initial value: 0", SEQUENCE_NAME);
                    return sequenceRepository.save(newSeq);
                });

        long nextVal = sequence.getCurrentValue() + 1;
        String code = ClientCodeGenerator.generate(nextVal);

        while (clientRepository.existsByClientCode(code)) {
            log.warn("Client code {} already exists in database. Incrementing sequence counter.", code);
            nextVal++;
            code = ClientCodeGenerator.generate(nextVal);
        }

        sequence.setCurrentValue(nextVal);
        sequenceRepository.save(sequence);

        log.debug("Generated concurrency-safe client code: {}", code);
        return code;
    }
}
