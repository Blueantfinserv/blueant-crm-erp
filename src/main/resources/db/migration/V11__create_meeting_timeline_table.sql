-- ============================================================================
-- Migration: Create meeting_timeline table
-- ============================================================================

CREATE TABLE meeting_timeline (
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
    meeting_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    meeting_sequence INT,
    meeting_outcome VARCHAR(50),
    previous_status VARCHAR(50),
    current_status VARCHAR(50),
    description VARCHAR(1000),

    CONSTRAINT fk_timeline_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_timeline_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id)
);

CREATE INDEX idx_timeline_lead ON meeting_timeline(lead_id);
CREATE INDEX idx_timeline_meeting ON meeting_timeline(meeting_id);
