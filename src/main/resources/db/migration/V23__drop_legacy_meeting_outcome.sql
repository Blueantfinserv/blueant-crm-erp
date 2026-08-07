-- V23__drop_legacy_meeting_outcome.sql
-- Drop the deprecated meeting_outcome column to enforce the redesigned workflow as the single source of truth.

ALTER TABLE meetings DROP COLUMN meeting_outcome;
ALTER TABLE meeting_updates DROP COLUMN meeting_outcome;
