-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V34
-- Description: Add physical lead assignment fields to leads table
-- ==============================================================================

ALTER TABLE leads
    ADD COLUMN speciality VARCHAR(150) NULL,
    ADD COLUMN clinic_address VARCHAR(255) NULL,
    ADD COLUMN is_physical_lead BIT NOT NULL DEFAULT 0,
    ADD COLUMN assignment_source VARCHAR(50) NULL,
    ADD COLUMN assigned_by_user_id BIGINT NULL,
    ADD COLUMN assigned_at DATETIME(6) NULL;

ALTER TABLE leads
    ADD CONSTRAINT fk_lead_assigned_by
    FOREIGN KEY (assigned_by_user_id) REFERENCES users (id);

CREATE INDEX idx_lead_is_physical ON leads (is_physical_lead);
CREATE INDEX idx_lead_assigned_by ON leads (assigned_by_user_id);
