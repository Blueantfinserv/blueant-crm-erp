package com.blueant_crm_erp.lead.service;

import java.time.LocalDate;
import java.util.Optional;

public interface LeadActivityTrackerService {

    /**
     * Retrieves the date of the last salesperson activity for a given lead.
     * In the future, this will query the LeadActivity module.
     *
     * @param uniqueLeadId the unique lead ID
     * @return an Optional containing the LocalDate of the last activity, or empty if not available
     */
    Optional<LocalDate> getLastActivityDate(String uniqueLeadId);
}
