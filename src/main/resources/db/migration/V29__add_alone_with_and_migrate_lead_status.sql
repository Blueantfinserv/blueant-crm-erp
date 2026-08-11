-- ============================================================================
-- Migration: V29 — Add alone_with fields to meeting_updates & migrate lead status
-- ============================================================================

-- Add alone_with audit fields to meeting_updates table to match meetings table
ALTER TABLE meeting_updates
    ADD COLUMN alone_with VARCHAR(20) DEFAULT NULL,
    ADD COLUMN person_name VARCHAR(100) DEFAULT NULL,
    ADD COLUMN position VARCHAR(100) DEFAULT NULL;

-- Migrate REMOVE_CLIENT to CLIENT_REMOVED for consistency with the new enums
UPDATE meetings SET lead_status = 'CLIENT_REMOVED' WHERE lead_status = 'REMOVE_CLIENT';
UPDATE meeting_updates SET lead_status = 'CLIENT_REMOVED' WHERE lead_status = 'REMOVE_CLIENT';
