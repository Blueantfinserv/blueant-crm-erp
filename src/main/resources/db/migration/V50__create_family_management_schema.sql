-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V50
-- Description: Create Family Management & CRM-Controlled Family Head Schema
--              1. Create client_families table
--              2. Create client_family_members table (with active membership uniqueness)
--              3. Create client_family_head_histories table
--              4. Add family_id foreign key column to clients table
--              5. Seed FAMILY_CODE in lead_code_sequences
-- ==============================================================================

-- 1. Create client_families table
CREATE TABLE client_families (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    family_code                 VARCHAR(50)     NOT NULL,
    family_name                 VARCHAR(150)    NOT NULL,
    family_head_client_id       BIGINT          NULL,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

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
    CONSTRAINT uk_client_family_code UNIQUE (family_code),
    CONSTRAINT fk_cl_family_head FOREIGN KEY (family_head_client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_family_code ON client_families(family_code);
CREATE INDEX idx_cl_family_head ON client_families(family_head_client_id);
CREATE INDEX idx_cl_family_status ON client_families(status);

-- 2. Create client_family_members table
CREATE TABLE client_family_members (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    family_id                   BIGINT          NOT NULL,
    client_id                   BIGINT          NOT NULL,
    relationship_to_head        VARCHAR(50)     NULL,
    membership_status           VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    joined_at                   DATETIME        NOT NULL,
    joined_by                   VARCHAR(100)    NOT NULL,
    left_at                     DATETIME        NULL,
    left_by                     VARCHAR(100)    NULL,

    -- Enforce conditional uniqueness: A client can only belong to ONE ACTIVE family across the database
    active_client_id            BIGINT GENERATED ALWAYS AS (
                                    CASE WHEN membership_status = 'ACTIVE' AND is_deleted = 0 
                                         THEN client_id 
                                         ELSE NULL 
                                    END
                                ) STORED,

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
    CONSTRAINT uk_cl_fammem_active_client UNIQUE (active_client_id),
    CONSTRAINT fk_cl_fammem_family FOREIGN KEY (family_id) REFERENCES client_families(id),
    CONSTRAINT fk_cl_fammem_client FOREIGN KEY (client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_fammem_family ON client_family_members(family_id);
CREATE INDEX idx_cl_fammem_client ON client_family_members(client_id);
CREATE INDEX idx_cl_fammem_status ON client_family_members(membership_status);

-- 3. Create client_family_head_histories table (Immutable audit log)
CREATE TABLE client_family_head_histories (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    family_id                   BIGINT          NOT NULL,
    previous_head_client_id     BIGINT          NULL,
    new_head_client_id          BIGINT          NOT NULL,
    changed_by_user             VARCHAR(100)    NOT NULL,
    changed_at                  DATETIME        NOT NULL,
    reason                      VARCHAR(500)    NULL,

    -- Base Audit
    created_at                  DATETIME        NOT NULL,
    created_by                  VARCHAR(100)    NOT NULL,
    updated_at                  DATETIME        NULL,
    updated_by                  VARCHAR(100)    NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_cl_famhist_family FOREIGN KEY (family_id) REFERENCES client_families(id),
    CONSTRAINT fk_cl_famhist_prevhead FOREIGN KEY (previous_head_client_id) REFERENCES clients(id),
    CONSTRAINT fk_cl_famhist_newhead FOREIGN KEY (new_head_client_id) REFERENCES clients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_famhist_family ON client_family_head_histories(family_id);
CREATE INDEX idx_cl_famhist_newhead ON client_family_head_histories(new_head_client_id);

-- 4. Add family_id column to clients table for direct access
ALTER TABLE clients
    ADD COLUMN family_id BIGINT NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_client_family FOREIGN KEY (family_id) REFERENCES client_families(id);

CREATE INDEX idx_client_family ON clients(family_id);

-- 5. Seed FAMILY_CODE in lead_code_sequences
INSERT INTO lead_code_sequences (sequence_name, current_value)
VALUES ('FAMILY_CODE', 0)
ON DUPLICATE KEY UPDATE sequence_name = sequence_name;
