-- ============================================================================
-- Migration: Create Meetings Table
-- ============================================================================

CREATE TABLE meetings (
    -- Base Entity
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

    -- Meeting Entity
    meeting_code VARCHAR(30) NOT NULL,
    meeting_number INT NOT NULL,
    lead_id BIGINT,
    assigned_employee_id BIGINT,
    meeting_mode VARCHAR(30) NOT NULL,
    meeting_date DATE NOT NULL,
    meeting_time TIME NOT NULL,
    meeting_location VARCHAR(255),
    agenda VARCHAR(500),
    remarks VARCHAR(1000),
    meeting_status VARCHAR(30) NOT NULL,
    meeting_outcome VARCHAR(30),
    status VARCHAR(20) NOT NULL,

    -- Constraints
    CONSTRAINT uk_meeting_code UNIQUE (meeting_code),
    CONSTRAINT fk_meeting_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_meeting_employee FOREIGN KEY (assigned_employee_id) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_meeting_status ON meetings(meeting_status);
CREATE INDEX idx_meeting_date ON meetings(meeting_date);
CREATE INDEX idx_meeting_lead ON meetings(lead_id);
CREATE INDEX idx_meeting_employee ON meetings(assigned_employee_id);
