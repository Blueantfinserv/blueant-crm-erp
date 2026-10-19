package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
import com.blueant_crm_erp.client.service.FamilyCodeGeneratorService;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.util.id.FamilyCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyCodeGeneratorServiceImpl implements FamilyCodeGeneratorService {

    private final LeadCodeSequenceRepository sequenceRepository;
    private final ClientFamilyRepository clientFamilyRepository;

    public static final String SEQUENCE_NAME = "FAMILY_CODE";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextFamilyCode() {
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
        String code = FamilyCodeGenerator.generate(nextVal);

        while (clientFamilyRepository.existsByFamilyCode(code)) {
            log.warn("Family code {} already exists in database. Incrementing sequence counter.", code);
            nextVal++;
            code = FamilyCodeGenerator.generate(nextVal);
        }

        sequence.setCurrentValue(nextVal);
        sequenceRepository.save(sequence);

        log.debug("Generated concurrency-safe family code: {}", code);
        return code;
    }
}
