-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V49
-- Description: Extend Client Profile with Personal, Source, Investment,
--              Nominee, Document Checklist, and Assignment History.
-- ==============================================================================

-- 1. Widen existing investment_type and add Client Profile columns to clients table
ALTER TABLE clients
    MODIFY COLUMN investment_type VARCHAR(255) NULL;

ALTER TABLE clients
    -- Section A: Personal & Family Details
    ADD COLUMN date_of_birth DATE NULL,
    ADD COLUMN mother_name VARCHAR(150) NULL,
    ADD COLUMN place_of_birth VARCHAR(100) NULL,
    ADD COLUMN occupation VARCHAR(100) NULL,
    ADD COLUMN family_head_name VARCHAR(150) NULL,
    ADD COLUMN family_details VARCHAR(1000) NULL,
    ADD COLUMN residential_address VARCHAR(500) NULL,
    ADD COLUMN correspondence_address VARCHAR(500) NULL,
    ADD COLUMN office_address VARCHAR(500) NULL,
    ADD COLUMN city VARCHAR(100) NULL,
    ADD COLUMN state VARCHAR(100) NULL,
    ADD COLUMN pin_code VARCHAR(20) NULL,

    -- Section B: Client Source & Ownership
    ADD COLUMN client_source VARCHAR(100) NULL,
    ADD COLUMN source_description VARCHAR(500) NULL,
    ADD COLUMN created_by_role VARCHAR(50) NULL,
    ADD COLUMN previous_sales_person_id BIGINT NULL,

    -- Section C: Investment & Application Details
    ADD COLUMN application_received_date DATE NULL,
    ADD COLUMN application_mode VARCHAR(50) NULL,
    ADD COLUMN first_investment_amount DECIMAL(15, 2) NULL,
    ADD COLUMN expected_max_sip DECIMAL(15, 2) NULL,
    ADD COLUMN investwell_id VARCHAR(100) NULL,
    ADD COLUMN client_report_date DATE NULL,
    ADD COLUMN payment_status VARCHAR(50) NULL,
    ADD COLUMN payment_amount DECIMAL(15, 2) NULL,
    ADD COLUMN payment_date DATE NULL,
    ADD COLUMN payment_reference VARCHAR(100) NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_client_prev_sales_person FOREIGN KEY (previous_sales_person_id) REFERENCES users(id);

CREATE INDEX idx_client_city ON clients(city);
CREATE INDEX idx_client_state ON clients(state);
CREATE INDEX idx_client_source ON clients(client_source);

-- 2. Create client_nominees table (Section D)
CREATE TABLE client_nominees (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    client_id               BIGINT          NOT NULL,
    nominee_name            VARCHAR(150)    NOT NULL,
    relationship            VARCHAR(50)     NOT NULL,
    date_of_birth           DATE            NULL,
    contact_number          VARCHAR(20)     NULL,
    pan_number              VARCHAR(20)     NULL,
    is_minor                TINYINT(1)      NOT NULL DEFAULT 0,
    guardian_name           VARCHAR(150)    NULL,
    guardian_relationship   VARCHAR(50)     NULL,
    guardian_contact        VARCHAR(20)     NULL,
    guardian_pan            VARCHAR(20)     NULL,

    -- Base Audit & Soft Delete & Versioning
    created_at              DATETIME        NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_at              DATETIME        NULL,
    updated_by              VARCHAR(100)    NULL,
    is_deleted              TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at              DATETIME        NULL,
    deleted_by              VARCHAR(100)    NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_cl_nominee_client FOREIGN KEY (client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_nominee_client ON client_nominees(client_id);

-- 3. Create client_documents table (Section E)
CREATE TABLE client_documents (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    client_id               BIGINT          NOT NULL,
    document_type           VARCHAR(100)    NOT NULL,
    document_name           VARCHAR(150)    NULL,
    received                TINYINT(1)      NOT NULL DEFAULT 0,
    received_date           DATE            NULL,
    verification_status     VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    document_reference      VARCHAR(255)    NULL,
    remarks                 VARCHAR(500)    NULL,

    -- Base Audit & Soft Delete & Versioning
    created_at              DATETIME        NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_at              DATETIME        NULL,
    updated_by              VARCHAR(100)    NULL,
    is_deleted              TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at              DATETIME        NULL,
    deleted_by              VARCHAR(100)    NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_cl_doc_client FOREIGN KEY (client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_doc_client ON client_documents(client_id);
CREATE INDEX idx_cl_doc_type ON client_documents(document_type);
CREATE INDEX idx_cl_doc_verif_status ON client_documents(verification_status);

-- 4. Create client_assignment_histories table (Ownership & Assignment Audit)
CREATE TABLE client_assignment_histories (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    client_id               BIGINT          NOT NULL,
    assigned_from_user_id   BIGINT          NULL,
    assigned_to_user_id     BIGINT          NOT NULL,
    assigned_by_user_id     BIGINT          NOT NULL,
    assignment_date         DATE            NOT NULL,
    assigned_at             DATETIME        NOT NULL,
    reason                  VARCHAR(500)    NULL,

    -- Base Audit
    created_at              DATETIME        NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_at              DATETIME        NULL,
    updated_by              VARCHAR(100)    NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_cl_hist_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_cl_hist_from_user FOREIGN KEY (assigned_from_user_id) REFERENCES users(id),
    CONSTRAINT fk_cl_hist_to_user FOREIGN KEY (assigned_to_user_id) REFERENCES users(id),
    CONSTRAINT fk_cl_hist_by_user FOREIGN KEY (assigned_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_hist_client ON client_assignment_histories(client_id);
CREATE INDEX idx_cl_hist_to_user ON client_assignment_histories(assigned_to_user_id);
