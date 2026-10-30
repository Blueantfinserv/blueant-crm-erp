-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V54
-- Description: Create client_import_jobs table for asynchronous bulk client
--              import lifecycle tracking, progress monitoring, and restart recovery.
-- ==============================================================================

CREATE TABLE client_import_jobs (
    id                      VARCHAR(36)     NOT NULL,
    file_name               VARCHAR(255)    NOT NULL,
    file_hash_sha256        VARCHAR(64)     NOT NULL,
    total_rows              INT             NOT NULL DEFAULT 0,
    processed_rows          INT             NOT NULL DEFAULT 0,
    status                  VARCHAR(30)     NOT NULL,
    error_message           VARCHAR(1000)   NULL,
    error_details_json      LONGTEXT        NULL,
    start_client_code       VARCHAR(50)     NULL,
    end_client_code         VARCHAR(50)     NULL,
    created_by              VARCHAR(100)    NOT NULL,
    created_at              DATETIME        NOT NULL,
    started_at              DATETIME        NULL,
    heartbeat_at            DATETIME        NULL,
    completed_at            DATETIME        NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_client_import_status ON client_import_jobs(status);
CREATE INDEX idx_client_import_hash ON client_import_jobs(file_hash_sha256);
CREATE INDEX idx_client_import_created_by ON client_import_jobs(created_by);

