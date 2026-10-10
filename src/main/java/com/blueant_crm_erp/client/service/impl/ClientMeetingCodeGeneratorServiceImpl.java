package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.service.ClientMeetingCodeGeneratorService;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientMeetingCodeGeneratorServiceImpl implements ClientMeetingCodeGeneratorService {

    private final LeadCodeSequenceRepository sequenceRepository;
    private final ClientMeetingRepository clientMeetingRepository;

    public static final String SEQUENCE_NAME = "CLIENT_MEETING_CODE";
    private static final String PREFIX = "BA-CLM";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextMeetingCode() {
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
        int year = LocalDate.now().getYear();
        String code = formatCode(year, nextVal);

        while (clientMeetingRepository.existsByMeetingCode(code)) {
            log.warn("Client meeting code {} already exists in database. Incrementing sequence counter.", code);
            nextVal++;
            code = formatCode(year, nextVal);
        }

        sequence.setCurrentValue(nextVal);
        sequenceRepository.save(sequence);

        log.debug("Generated concurrency-safe client meeting code: {}", code);
        return code;
    }

    private String formatCode(int year, long sequence) {
        return String.format("%s-%d-%06d", PREFIX, year, sequence);
    }
}
