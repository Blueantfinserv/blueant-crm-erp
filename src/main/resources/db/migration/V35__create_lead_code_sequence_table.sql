-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V35
-- Description: Create lead code sequence table for concurrency-safe generation
-- ==============================================================================

CREATE TABLE lead_code_sequences (
    sequence_name VARCHAR(50) NOT NULL,
    current_value BIGINT NOT NULL,
    PRIMARY KEY (sequence_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default LEAD_CODE sequence using MAX existing numeric LD code in leads table (minimum 1000)
INSERT INTO lead_code_sequences (sequence_name, current_value)
SELECT 'LEAD_CODE', GREATEST(1000, COALESCE(MAX(CAST(SUBSTRING(lead_code, 3) AS UNSIGNED)), 1000))
FROM leads
WHERE lead_code LIKE 'LD%' AND SUBSTRING(lead_code, 3) REGEXP '^[0-9]+$'
ON DUPLICATE KEY UPDATE sequence_name = sequence_name;
