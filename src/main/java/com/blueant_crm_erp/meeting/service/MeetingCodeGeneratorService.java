package com.blueant_crm_erp.meeting.service;

public interface MeetingCodeGeneratorService {

    /**
     * Generates the next globally unique, concurrency-safe meeting code.
     * Uses pessimistic write locking and an independent transaction to ensure uniqueness.
     *
     * @return Unique meeting code (e.g. BA-MTG-2026-000537)
     */
    String generateNextMeetingCode();
}
