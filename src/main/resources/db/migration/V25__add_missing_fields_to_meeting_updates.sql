-- ============================================================================
-- Migration: Add missing workflow and GPS fields to meeting_updates table
-- ============================================================================

ALTER TABLE meeting_updates
    ADD COLUMN reason VARCHAR(255) NULL,
    ADD COLUMN next_plan_time TIME NULL,
    ADD COLUMN current_investment_company VARCHAR(150) NULL,
    ADD COLUMN current_advisor VARCHAR(150) NULL,
    ADD COLUMN investment_type VARCHAR(50) NULL,
    ADD COLUMN investment_company VARCHAR(150) NULL,
    ADD COLUMN current_stage VARCHAR(100) NULL,
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL,
    ADD COLUMN location_captured_at DATETIME NULL,
    ADD COLUMN location_accuracy DOUBLE NULL;
