package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingCodeGeneratorService;
import com.blueant_crm_erp.util.id.MeetingCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingCodeGeneratorServiceImpl implements MeetingCodeGeneratorService {

    private final LeadCodeSequenceRepository sequenceRepository;
    private final MeetingRepository meetingRepository;

    public static final String SEQUENCE_NAME = "MEETING_CODE";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextMeetingCode() {
        LeadCodeSequence sequence = sequenceRepository.findBySequenceNameWithLock(SEQUENCE_NAME)
                .orElseGet(() -> {
                    long initialSeq = getMaximumExistingMeetingSequence();
                    LeadCodeSequence newSeq = LeadCodeSequence.builder()
                            .sequenceName(SEQUENCE_NAME)
                            .currentValue(initialSeq)
                            .build();
                    log.info("Initialized sequence {} with initial value: {}", SEQUENCE_NAME, initialSeq);
                    return sequenceRepository.save(newSeq);
                });

        long nextVal = sequence.getCurrentValue() + 1;
        String code = MeetingCodeGenerator.generate(nextVal);

        while (meetingRepository.existsByMeetingCode(code)) {
            log.warn("Meeting code {} already exists in database. Incrementing sequence counter.", code);
            nextVal++;
            code = MeetingCodeGenerator.generate(nextVal);
        }

        sequence.setCurrentValue(nextVal);
        sequenceRepository.save(sequence);

        log.debug("Generated concurrency-safe meeting code: {}", code);
        return code;
    }

    private long getMaximumExistingMeetingSequence() {
        try {
            Long maxSeq = meetingRepository.findMaxMeetingCodeSequence();
            if (maxSeq != null && maxSeq > 0) {
                return maxSeq;
            }
        } catch (Exception e) {
            log.warn("Could not query maximum meeting code sequence directly: {}. Falling back to count.", e.getMessage());
        }
        return meetingRepository.count();
    }
}
