-- ============================================================================
-- Migration: Add fields for extended Meeting Update workflow and Live GPS
-- ============================================================================

ALTER TABLE meetings
    ADD COLUMN meeting_conducted TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN reason VARCHAR(255) NULL,
    ADD COLUMN current_investment_company VARCHAR(150) NULL,
    ADD COLUMN current_advisor VARCHAR(150) NULL,
    ADD COLUMN investment_type VARCHAR(50) NULL,
    ADD COLUMN investment_company VARCHAR(150) NULL,
    ADD COLUMN current_stage VARCHAR(100) NULL,
    ADD COLUMN pan_number VARCHAR(20) NULL,
    ADD COLUMN investment_amount DECIMAL(15,2) NULL,
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL,
    ADD COLUMN location_captured_at DATETIME NULL,
    ADD COLUMN location_accuracy DOUBLE NULL;

ALTER TABLE meeting_updates
    ADD COLUMN reason VARCHAR(255) NULL,
    ADD COLUMN next_plan_time TIME NULL,
    ADD COLUMN current_investment_company VARCHAR(150) NULL,
    ADD COLUMN current_advisor VARCHAR(150) NULL,
    ADD COLUMN investment_type VARCHAR(50) NULL,
    ADD COLUMN investment_company VARCHAR(150) NULL,
    ADD COLUMN current_stage VARCHAR(100) NULL,
    ADD COLUMN address VARCHAR(500) NULL,
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL,
    ADD COLUMN location_captured_at DATETIME NULL,
    ADD COLUMN location_accuracy DOUBLE NULL;
