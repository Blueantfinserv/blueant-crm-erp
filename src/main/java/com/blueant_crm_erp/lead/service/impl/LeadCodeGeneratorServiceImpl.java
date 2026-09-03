package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.constants.LeadConstants;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadCodeGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadCodeGeneratorServiceImpl implements LeadCodeGeneratorService {

    private final LeadCodeSequenceRepository leadCodeSequenceRepository;
    private final LeadRepository leadRepository;

    private static final String SEQUENCE_NAME = "LEAD_CODE";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextLeadCode() {
        LeadCodeSequence sequence = leadCodeSequenceRepository.findBySequenceNameWithLock(SEQUENCE_NAME)
                .orElseGet(() -> {
                    LeadCodeSequence newSeq = LeadCodeSequence.builder()
                            .sequenceName(SEQUENCE_NAME)
                            .currentValue(1000L)
                            .build();
                    return leadCodeSequenceRepository.save(newSeq);
                });

        long nextVal = sequence.getCurrentValue() + 1;
        String code = LeadConstants.LEAD_CODE_PREFIX + String.format("%0" + LeadConstants.LEAD_CODE_PADDING + "d", nextVal);

        while (leadRepository.existsByLeadCode(code)) {
            nextVal++;
            code = LeadConstants.LEAD_CODE_PREFIX + String.format("%0" + LeadConstants.LEAD_CODE_PADDING + "d", nextVal);
        }

        sequence.setCurrentValue(nextVal);
        leadCodeSequenceRepository.save(sequence);

        log.debug("Generated concurrency-safe lead code: {}", code);
        return code;
    }
}
