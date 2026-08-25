-- ============================================================================
-- Migration: Add new coordinator verification questionnaire fields
-- ============================================================================
ALTER TABLE meeting_verifications
    ADD COLUMN meeting_timing TIME NULL,
    ADD COLUMN age_group VARCHAR(50) NULL,
    ADD COLUMN existing_sip VARCHAR(50) NULL,
    ADD COLUMN profession_detail VARCHAR(255) NULL,
    ADD COLUMN best_time_for_meeting VARCHAR(50) NULL;
