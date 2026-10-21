-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V52
-- Description: Add business_types column to client_meetings and
--              client_meeting_verifications for multi-select PC business types
-- ==============================================================================

ALTER TABLE client_meetings
    ADD COLUMN business_types VARCHAR(500) NULL AFTER business_generated;

ALTER TABLE client_meeting_verifications
    ADD COLUMN business_types VARCHAR(500) NULL AFTER investment_type;
