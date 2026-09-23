-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V43
-- Description: Add created_by_sales_person_id to clients table,
--              create crm_onboardings table for Questions 1-28 and payment decisions,
--              create client_followups table for ~3-month recurring follow-ups,
--              and link crm_verifications to crm_onboardings.
-- ==============================================================================

-- 1. Extend clients table with immutable created_by_sales_person_id
ALTER TABLE clients
    ADD COLUMN created_by_sales_person_id BIGINT NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_client_created_by_sales_person FOREIGN KEY (created_by_sales_person_id) REFERENCES users(id);

UPDATE clients
    SET created_by_sales_person_id = sales_person_id
    WHERE created_by_sales_person_id IS NULL AND sales_person_id IS NOT NULL;

-- 2. Create crm_onboardings table
CREATE TABLE crm_onboardings (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    lead_id                     BIGINT          NOT NULL UNIQUE,
    onboarding_status           VARCHAR(50)     NOT NULL,
    created_by_sales_person_id  BIGINT          NOT NULL,

    -- Questions 1 to 28
    investor_name               VARCHAR(150)    NOT NULL,
    is_blueant_investor         TINYINT(1)      NULL DEFAULT 0,
    family_head                 VARCHAR(150)    NULL,
    occupation                  VARCHAR(100)    NULL,
    -- Question 5 is mapped by created_by_sales_person_id
    pan_number                  VARCHAR(20)     NULL,
    contact_detail              VARCHAR(30)     NULL,
    mail_id                     VARCHAR(150)    NULL,
    correspondence_address      VARCHAR(500)    NULL,
    office_address              VARCHAR(500)    NULL,
    place_of_birth              VARCHAR(100)    NULL,
    family_details              VARCHAR(1000)   NULL,
    location                    VARCHAR(150)    NULL,
    source                      VARCHAR(100)    NULL,
    source_description         VARCHAR(500)    NULL,
    application_received_date   DATE            NULL,
    nominee_details             VARCHAR(500)    NULL,
    nominee_pan_or_aadhaar      VARCHAR(50)     NULL,
    mother_name                 VARCHAR(150)    NULL,
    application_mode            VARCHAR(50)     NULL,
    first_investment_amount     DECIMAL(15,2)   NULL,
    expected_max_sip            DECIMAL(15,2)   NULL,
    investment_type             VARCHAR(100)    NULL,
    all_documents_completed     TINYINT(1)      NULL DEFAULT 0,
    investwell_user_id          VARCHAR(100)    NULL,
    helpdesk_query_no           VARCHAR(100)    NULL,
    client_reported_date        DATE            NULL,
    payment_done                TINYINT(1)      NULL DEFAULT 0,

    remarks                     VARCHAR(1000)   NULL,
    submitted_at                DATETIME        NULL,
    submitted_by                VARCHAR(100)    NULL,

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
    CONSTRAINT fk_crm_onboarding_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_crm_onboarding_sales_person FOREIGN KEY (created_by_sales_person_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_crm_onboard_lead ON crm_onboardings(lead_id);
CREATE INDEX idx_crm_onboard_status ON crm_onboardings(onboarding_status);

-- 3. Create client_followups table for recurring ~3-month follow-up logs
CREATE TABLE client_followups (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    client_id                   BIGINT          NOT NULL,
    sales_person_id             BIGINT          NOT NULL,
    followup_date               DATE            NOT NULL,
    remarks                     VARCHAR(1000)   NULL,
    next_followup_date          DATE            NOT NULL,

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
    CONSTRAINT fk_client_followup_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_client_followup_sales_person FOREIGN KEY (sales_person_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_client_followup_client ON client_followups(client_id);
CREATE INDEX idx_client_followup_sp ON client_followups(sales_person_id);

-- 4. Extend crm_verifications table with crm_onboarding_id
ALTER TABLE crm_verifications
    ADD COLUMN crm_onboarding_id BIGINT NULL;

ALTER TABLE crm_verifications
    ADD CONSTRAINT fk_crm_verif_onboarding FOREIGN KEY (crm_onboarding_id) REFERENCES crm_onboardings(id);
