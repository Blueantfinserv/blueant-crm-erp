-- ============================================================================
-- Migration: Create meeting_verifications table
-- ============================================================================
CREATE TABLE meeting_verifications (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    meeting_id              BIGINT          NOT NULL,
    verification_status     VARCHAR(50)     NOT NULL,
    verified_by             VARCHAR(100)    NULL,
    verified_at             DATETIME        NULL,
    rejection_reason        VARCHAR(1000)   NULL,
    
    -- Coordinator collected information
    alone_with              VARCHAR(20)     NULL,
    person_name             VARCHAR(100)    NULL,
    position                VARCHAR(100)    NULL,
    client_age              INT             NULL,
    marital_status          VARCHAR(50)     NULL,
    profession              VARCHAR(100)    NULL,
    email                   VARCHAR(150)    NULL,
    company_name            VARCHAR(150)    NULL,
    any_children            TINYINT(1)      NULL,
    number_of_children      INT             NULL,
    previous_investment     TINYINT(1)      NULL,
    
    -- Base Soft Delete & Versioning & Audit
    created_at              DATETIME        NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_at              DATETIME        NULL,
    updated_by              VARCHAR(100)    NULL,
    is_deleted              TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at              DATETIME        NULL,
    deleted_by              VARCHAR(100)    NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT uk_meeting_verification_meeting UNIQUE (meeting_id),
    CONSTRAINT fk_meeting_verification_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
