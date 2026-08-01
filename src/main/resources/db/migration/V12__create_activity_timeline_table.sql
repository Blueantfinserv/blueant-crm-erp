-- ============================================================================
-- Migration: Create activity_timeline table and drop meeting_timeline
-- ============================================================================

-- Drop the old table if it exists
DROP TABLE IF EXISTS meeting_timeline;

-- Create the new generic activity timeline table
CREATE TABLE activity_timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Base Audit Entity
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_at DATETIME,
    
    -- Base Soft Delete Entity
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    
    -- Base Version Entity
    version BIGINT NOT NULL,

    -- Timeline specifics
    lead_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL, -- MEETING, CALL, TASK, EMAIL, WHATSAPP, PROPOSAL, DOCUMENT, NOTE
    reference_id BIGINT,                -- ID of the related meeting, call, task, etc.
    title VARCHAR(255),
    description VARCHAR(1000),
    status VARCHAR(50),
    
    -- Workflow / Audit specific data (JSON or individual columns, keeping individual for simplicity based on previous)
    sequence_number INT,
    outcome VARCHAR(50),
    previous_status VARCHAR(50),
    current_status VARCHAR(50),

    CONSTRAINT fk_activity_timeline_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
);

CREATE INDEX idx_activity_timeline_lead ON activity_timeline(lead_id);
CREATE INDEX idx_activity_timeline_type ON activity_timeline(activity_type);
CREATE INDEX idx_activity_timeline_ref ON activity_timeline(reference_id);
