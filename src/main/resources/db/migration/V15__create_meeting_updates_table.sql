-- ============================================================================
-- Migration: Create meeting_updates table
-- ============================================================================
-- Every sales meeting update is persisted as an immutable audit record.
-- The meetings table retains denormalized latest values for fast reads.
-- ============================================================================

CREATE TABLE meeting_updates (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    meeting_id       BIGINT         NOT NULL,
    update_number    INT            NOT NULL,

    -- Meeting context captured at time of update
    meeting_date     DATE           NULL,
    meeting_time     TIME           NULL,
    meeting_mode     VARCHAR(30)    NULL,
    meeting_conducted TINYINT(1)    NOT NULL DEFAULT 0,

    -- Sales workflow fields
    completed_stage  VARCHAR(50)    NULL,
    lead_status      VARCHAR(50)    NULL,
    client_status    VARCHAR(50)    NULL,
    remarks          TEXT           NULL,
    joined_meeting_with VARCHAR(255) NULL,
    leader_name      VARCHAR(100)   NULL,
    next_plan_date   DATE           NULL,

    -- Investment fields
    pan_number       VARCHAR(20)    NULL,
    investment_amount DECIMAL(15,2) NULL,
    product_type     VARCHAR(100)   NULL,

    -- Outcome
    meeting_outcome  VARCHAR(30)    NULL,
    discussion       TEXT           NULL,

    -- Audit
    submitted_by     VARCHAR(100)   NOT NULL,
    submitted_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100)   NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(100)   NULL,
    updated_at       DATETIME       NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_meeting_update_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id),
    CONSTRAINT uk_meeting_update_number UNIQUE (meeting_id, update_number),
    INDEX idx_meeting_update_meeting (meeting_id),
    INDEX idx_meeting_update_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
