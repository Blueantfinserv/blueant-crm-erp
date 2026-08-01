-- ============================================================================
-- Migration: Add missing fields to meetings table
-- ============================================================================

ALTER TABLE meetings
ADD COLUMN meeting_type VARCHAR(30) NOT NULL DEFAULT 'IN_PERSON',
ADD COLUMN next_meeting_date DATE,
ADD COLUMN next_meeting_time TIME;