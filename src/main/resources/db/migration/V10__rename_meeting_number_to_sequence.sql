-- ============================================================================
-- Migration: Rename meeting_number to meeting_sequence
-- ============================================================================

ALTER TABLE meetings CHANGE meeting_number meeting_sequence INT NOT NULL;
