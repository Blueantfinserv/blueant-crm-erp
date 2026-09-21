-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V40
-- Description: Add optional best_time_to_meet column to leads table
-- ==============================================================================

ALTER TABLE leads
    ADD COLUMN best_time_to_meet VARCHAR(30) NULL;
