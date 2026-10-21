-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V51
-- Description: Add actual_date_of_birth column to clients table
-- ==============================================================================

ALTER TABLE clients
    ADD COLUMN actual_date_of_birth DATE NULL AFTER date_of_birth;
