-- ============================================================================
-- Migration: Make meeting_time nullable in meetings table
-- ============================================================================
ALTER TABLE meetings MODIFY COLUMN meeting_time TIME NULL;
