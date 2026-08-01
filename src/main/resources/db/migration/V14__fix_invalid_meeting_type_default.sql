-- ============================================================================
-- Migration: Fix invalid meeting_type data and default
-- ============================================================================

-- 1. Fix the invalid default value for future inserts
ALTER TABLE meetings ALTER COLUMN meeting_type SET DEFAULT 'INTRO';

-- 2. Correct legacy records corrupted by V13 according to business logic
UPDATE meetings SET meeting_type = 'INTRO' WHERE meeting_sequence = 1 AND meeting_type = 'IN_PERSON';
UPDATE meetings SET meeting_type = 'FOLLOW_UP' WHERE meeting_sequence > 1 AND meeting_type = 'IN_PERSON';