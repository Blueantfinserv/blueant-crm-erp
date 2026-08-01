-- ============================================================================
-- V20: Add unique constraint on (lead_id, meeting_sequence)
-- ============================================================================
-- Ensures that no two meetings can share the same sequence number for a lead.
-- This is the database-level enforcement of the idempotency guard.
-- ============================================================================

ALTER TABLE meetings
    ADD CONSTRAINT uk_lead_meeting_sequence UNIQUE (lead_id, meeting_sequence);
