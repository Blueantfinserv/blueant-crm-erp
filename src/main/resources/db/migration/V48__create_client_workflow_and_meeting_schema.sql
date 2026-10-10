-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V48
-- Description: Implement Existing Client Workflow Schema
--              1. Make clients.lead_id nullable for external client onboarding
--              2. Add registration & assignment fields to clients table
--              3. Create client_meetings table with sequence uniqueness
--              4. Create client_meeting_updates audit table
--              5. Create client_meeting_verifications table for immutable attempt history
--              6. Seed CLIENT_CODE and CLIENT_MEETING_CODE sequences
-- ==============================================================================

-- 1. Modify clients table to support direct external registration
ALTER TABLE clients MODIFY COLUMN lead_id BIGINT NULL;

ALTER TABLE clients
    ADD COLUMN speciality VARCHAR(100) NULL,
    ADD COLUMN location VARCHAR(150) NULL,
    ADD COLUMN clinic_address VARCHAR(500) NULL,
    ADD COLUMN alternate_mobile_number VARCHAR(20) NULL,
    ADD COLUMN remarks VARCHAR(1000) NULL,
    ADD COLUMN assignment_date DATE NULL,
    ADD COLUMN best_time_to_meet VARCHAR(50) NULL,
    ADD COLUMN assigned_by_id BIGINT NULL,
    ADD COLUMN assigned_at DATETIME NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_client_assigned_by FOREIGN KEY (assigned_by_id) REFERENCES users(id);

CREATE INDEX idx_client_sales_person ON clients(sales_person_id);
CREATE INDEX idx_client_status ON clients(client_status);

-- 2. Create client_meetings table
CREATE TABLE client_meetings (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    meeting_code                VARCHAR(30)     NOT NULL,
    meeting_sequence            INT             NOT NULL,
    client_id                   BIGINT          NOT NULL,
    assigned_employee_id        BIGINT          NOT NULL,

    meeting_mode                VARCHAR(30)     NOT NULL,
    meeting_date                DATE            NOT NULL,
    meeting_time                TIME            NULL,
    meeting_location            VARCHAR(255)    NULL,

    meeting_status              VARCHAR(30)     NOT NULL,
    meeting_conducted           VARCHAR(30)     NOT NULL DEFAULT 'NOT_CONDUCTED',
    client_meeting_status       VARCHAR(50)     NULL,

    alone_with                  VARCHAR(20)     NULL,
    person_name                 VARCHAR(100)    NULL,
    position                    VARCHAR(100)    NULL,
    business_generated          VARCHAR(10)     NULL,
    remarks                     VARCHAR(1000)   NULL,

    next_plan_date              DATE            NULL,
    next_plan_time              TIME            NULL,

    latitude                    DECIMAL(10, 7)  NULL,
    longitude                   DECIMAL(10, 7)  NULL,
    location_accuracy           DOUBLE          NULL,
    location_captured_at        DATETIME        NULL,
    google_maps_url             VARCHAR(512)    NULL,

    verified_by_pc              TINYINT(1)      NOT NULL DEFAULT 0,

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
    CONSTRAINT uk_client_meeting_code UNIQUE (meeting_code),
    CONSTRAINT uk_client_meeting_sequence UNIQUE (client_id, meeting_sequence),
    CONSTRAINT fk_cl_meeting_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_cl_meeting_employee FOREIGN KEY (assigned_employee_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_meeting_client ON client_meetings(client_id);
CREATE INDEX idx_cl_meeting_employee ON client_meetings(assigned_employee_id);
CREATE INDEX idx_cl_meeting_status ON client_meetings(meeting_status);
CREATE INDEX idx_cl_meeting_date ON client_meetings(meeting_date);

-- 3. Create client_meeting_updates table (Immutable update history)
CREATE TABLE client_meeting_updates (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    client_meeting_id           BIGINT          NOT NULL,
    update_number               INT             NOT NULL,

    meeting_mode                VARCHAR(30)     NOT NULL,
    meeting_date                DATE            NOT NULL,
    meeting_time                TIME            NULL,
    meeting_conducted           VARCHAR(30)     NOT NULL,
    client_meeting_status       VARCHAR(50)     NULL,

    alone_with                  VARCHAR(20)     NULL,
    person_name                 VARCHAR(100)    NULL,
    position                    VARCHAR(100)    NULL,
    business_generated          VARCHAR(10)     NULL,
    remarks                     VARCHAR(1000)   NULL,

    next_plan_date              DATE            NULL,
    next_plan_time              TIME            NULL,

    latitude                    DECIMAL(10, 7)  NULL,
    longitude                   DECIMAL(10, 7)  NULL,
    location_accuracy           DOUBLE          NULL,

    -- Base Audit
    created_at                  DATETIME        NOT NULL,
    created_by                  VARCHAR(100)    NOT NULL,
    updated_at                  DATETIME        NULL,
    updated_by                  VARCHAR(100)    NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cl_meeting_update_seq UNIQUE (client_meeting_id, update_number),
    CONSTRAINT fk_cl_update_meeting FOREIGN KEY (client_meeting_id) REFERENCES client_meetings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_update_meeting ON client_meeting_updates(client_meeting_id);

-- 4. Create client_meeting_verifications table (Immutable verification attempt history)
CREATE TABLE client_meeting_verifications (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    client_meeting_id           BIGINT          NOT NULL,
    attempt_number              INT             NOT NULL,
    is_current                  TINYINT(1)      NOT NULL DEFAULT 1,
    verification_status         VARCHAR(50)     NOT NULL,

    verified_by                 VARCHAR(100)    NULL,
    verified_at                 DATETIME        NULL,
    rejection_reason            VARCHAR(1000)   NULL,

    -- Conditional Verification Fields
    new_meeting_date            DATE            NULL,
    meeting_time                TIME            NULL,
    blueant_app_used            VARCHAR(10)     NULL,
    alone_with                  VARCHAR(20)     NULL,
    person_name                 VARCHAR(100)    NULL,
    position                    VARCHAR(100)    NULL,
    business_generated          VARCHAR(10)     NULL,
    investment_type             VARCHAR(100)    NULL,
    investment_amount           DECIMAL(15, 2)  NULL,
    remarks                     VARCHAR(1000)   NULL,
    next_followup_date          DATE            NULL,

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
    CONSTRAINT uk_cl_meeting_verif_attempt UNIQUE (client_meeting_id, attempt_number),
    CONSTRAINT fk_cl_verif_meeting FOREIGN KEY (client_meeting_id) REFERENCES client_meetings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_verif_meeting ON client_meeting_verifications(client_meeting_id);
CREATE INDEX idx_cl_verif_status ON client_meeting_verifications(verification_status);
CREATE INDEX idx_cl_verif_current ON client_meeting_verifications(client_meeting_id, is_current);

-- 5. Seed CLIENT_CODE and CLIENT_MEETING_CODE in lead_code_sequences table
INSERT INTO lead_code_sequences (sequence_name, current_value)
VALUES ('CLIENT_CODE', 0)
ON DUPLICATE KEY UPDATE sequence_name = sequence_name;

INSERT INTO lead_code_sequences (sequence_name, current_value)
VALUES ('CLIENT_MEETING_CODE', 0)
ON DUPLICATE KEY UPDATE sequence_name = sequence_name;
