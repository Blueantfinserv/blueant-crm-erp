package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.lead.service.LeadActivityTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class LeadActivityTrackerServiceImpl implements LeadActivityTrackerService {

    @Override
    public Optional<LocalDate> getLastActivityDate(String uniqueLeadId) {
        // Functionality not yet implemented. Future LeadActivity module will provide this.
        log.warn("LeadActivity module is not yet implemented. Returning empty activity date for lead: {}", uniqueLeadId);
        return Optional.empty();
    }
}
