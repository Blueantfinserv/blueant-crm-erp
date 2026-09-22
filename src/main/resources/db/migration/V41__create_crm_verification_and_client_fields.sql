-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V41
-- Description: Add sales_person_id and next_followup_date to clients table,
--              and create crm_verifications table for generic CRM verification.
-- ==============================================================================

-- 1. Extend clients table with sales_person_id and next_followup_date
ALTER TABLE clients
    ADD COLUMN sales_person_id BIGINT NULL,
    ADD COLUMN next_followup_date DATE NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_client_sales_person FOREIGN KEY (sales_person_id) REFERENCES users(id);

-- 2. Create crm_verifications table
CREATE TABLE crm_verifications (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    lead_id                     BIGINT          NOT NULL,
    client_id                   BIGINT          NULL,
    verification_status         VARCHAR(50)     NOT NULL,
    
    -- Generic CRM Verification Questions / Checklist
    kyc_verified                TINYINT(1)      NULL DEFAULT 0,
    bank_details_verified       TINYINT(1)      NULL DEFAULT 0,
    documents_verified          TINYINT(1)      NULL DEFAULT 0,
    client_contact_confirmed    TINYINT(1)      NULL DEFAULT 0,
    pan_number                  VARCHAR(20)     NULL,
    remarks                     VARCHAR(1000)   NULL,
    
    verified_by                 VARCHAR(100)    NULL,
    verified_at                 DATETIME        NULL,
    rejection_reason            VARCHAR(1000)   NULL,
    
    -- Base Audit & Soft Delete & Versioning
    created_at                  DATETIME        NOT NULL,
    created_by                  VARCHAR(100)    NOT NULL,
    updated_at                  DATETIME        NULL,
    updated_by                  VARCHAR(100)    NULL,
    is_deleted                  TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at                  DATETIME        NULL,
    deleted_by                  VARCHAR(100)    NULL,
    version                     BIGINT          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_crm_verification_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_crm_verification_client FOREIGN KEY (client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_crm_verif_lead ON crm_verifications(lead_id);
CREATE INDEX idx_crm_verif_status ON crm_verifications(verification_status);
