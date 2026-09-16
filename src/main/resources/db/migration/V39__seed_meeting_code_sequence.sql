-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V39
-- Description: Seed meeting code sequence in sequence table for concurrency-safe generation
-- ==============================================================================

INSERT INTO lead_code_sequences (sequence_name, current_value)
SELECT 'MEETING_CODE', COALESCE(MAX(CAST(SUBSTRING(meeting_code, 13) AS UNSIGNED)), 0)
FROM meetings
WHERE meeting_code LIKE 'BA-MTG-%' AND LENGTH(meeting_code) >= 18 AND SUBSTRING(meeting_code, 13) REGEXP '^[0-9]+$'
ON DUPLICATE KEY UPDATE sequence_name = sequence_name;
