-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V38
-- Description: Add optional assignment_date column to leads table
-- ==============================================================================

ALTER TABLE leads
    ADD COLUMN assignment_date DATE NULL;
