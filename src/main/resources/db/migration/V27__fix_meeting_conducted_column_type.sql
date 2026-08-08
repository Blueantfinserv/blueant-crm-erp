-- ============================================================================
-- Migration: Modify meeting_conducted column type from BIT to VARCHAR(30)
-- ============================================================================

ALTER TABLE meeting_updates MODIFY COLUMN meeting_conducted VARCHAR(30) NULL;
UPDATE meeting_updates SET meeting_conducted = CASE WHEN meeting_conducted = '1' OR meeting_conducted = 'true' THEN 'CONDUCTED' ELSE 'NOT_CONDUCTED' END;
ALTER TABLE meeting_updates MODIFY COLUMN meeting_conducted VARCHAR(30) NOT NULL DEFAULT 'NOT_CONDUCTED';
