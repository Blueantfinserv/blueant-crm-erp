-- ============================================================================
-- Migration: Add missing google_maps_url column to meeting_updates table
-- ============================================================================

ALTER TABLE meeting_updates ADD COLUMN google_maps_url VARCHAR(512) NULL;
