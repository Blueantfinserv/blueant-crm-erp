package com.blueant_crm_erp.lead.service;

public interface LeadCodeGeneratorService {

    /**
     * Generates a concurrency-safe, unique lead code in LDxxxxxx format.
     * Uses pessimistic write locking on the database sequence counter table.
     *
     * @return unique lead code
     */
    String generateNextLeadCode();
}
