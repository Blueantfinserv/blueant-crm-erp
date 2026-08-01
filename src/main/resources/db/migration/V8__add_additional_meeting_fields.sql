-- ============================================================================
-- Migration: Add Additional Fields to Meetings Table
-- ============================================================================

ALTER TABLE meetings 
ADD COLUMN meeting_photo VARCHAR(500),
ADD COLUMN visiting_card VARCHAR(500),
ADD COLUMN meeting_notes VARCHAR(2000);
