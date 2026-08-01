-- V17__create_phase4_tables.sql
-- Description: Creates tables for FollowUp, Target, AuditLog, and modifies Meeting and Client.

-- ============================================================================
-- 1. Alter Meeting Table for Process Coordinator Verification
-- ============================================================================
ALTER TABLE meetings 
ADD COLUMN verified_by_process_coordinator BOOLEAN DEFAULT FALSE,
ADD COLUMN meeting_verification_date DATETIME,
ADD COLUMN verification_remarks VARCHAR(1000),
ADD COLUMN verified_by VARCHAR(100);

-- ============================================================================
-- 2. Alter Client Table for Detailed Investment & RM Tracking
-- ============================================================================
ALTER TABLE clients 
ADD COLUMN amc VARCHAR(100),
ADD COLUMN scheme VARCHAR(150),
ADD COLUMN investment_type VARCHAR(50),
ADD COLUMN client_since DATE,
ADD COLUMN relationship_manager_id BIGINT,
ADD COLUMN crm_owner_id BIGINT;

ALTER TABLE clients
ADD CONSTRAINT fk_client_rm FOREIGN KEY (relationship_manager_id) REFERENCES users(id),
ADD CONSTRAINT fk_client_crm_owner FOREIGN KEY (crm_owner_id) REFERENCES users(id);

-- ============================================================================
-- 3. Targets Table
-- ============================================================================
CREATE TABLE targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    user_id BIGINT NOT NULL,
    target_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    
    revenue_target DECIMAL(15,2) DEFAULT 0.00,
    meeting_target INT DEFAULT 0,
    lead_target INT DEFAULT 0,
    followup_target INT DEFAULT 0,
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_target_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_target_user_month UNIQUE (user_id, target_month)
);

-- ============================================================================
-- 4. Followups Table
-- ============================================================================
CREATE TABLE followups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    lead_id BIGINT NOT NULL,
    followup_date DATE NOT NULL,
    followup_time TIME NOT NULL,
    
    remarks VARCHAR(1000),
    reminder BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    completed_by VARCHAR(100),
    next_followup_date DATE,
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_followup_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
);

CREATE INDEX idx_followup_date ON followups(followup_date);
CREATE INDEX idx_followup_lead ON followups(lead_id);

-- ============================================================================
-- 5. Global Audit Logs Table
-- ============================================================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    
    who VARCHAR(100) NOT NULL,
    action_time DATETIME NOT NULL,
    
    old_value JSON,
    new_value JSON,
    
    ip_address VARCHAR(50),
    browser VARCHAR(200),
    device VARCHAR(100)
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_time ON audit_logs(action_time);

