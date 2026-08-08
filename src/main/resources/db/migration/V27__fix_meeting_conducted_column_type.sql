-- ============================================================================
-- V27: Fix meeting_conducted column type on meeting_updates
-- ============================================================================
-- SAFE NO-OP: meeting_conducted was already converted to VARCHAR(30) by V22.
-- This migration is retained to preserve Flyway version history.
-- ============================================================================

SELECT 1;
