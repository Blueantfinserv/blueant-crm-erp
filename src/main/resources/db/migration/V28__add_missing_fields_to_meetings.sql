-- ============================================================================
-- Migration: Add missing workflow and GPS fields to meetings table
-- ============================================================================

ALTER TABLE meetings
    ADD COLUMN meeting_conducted VARCHAR(30) NOT NULL DEFAULT 'NOT_CONDUCTED',
    ADD COLUMN lead_status VARCHAR(50) NULL,
    ADD COLUMN reason VARCHAR(255) NULL,
    ADD COLUMN current_investment_company VARCHAR(150) NULL,
    ADD COLUMN current_advisor VARCHAR(150) NULL,
    ADD COLUMN investment_type VARCHAR(50) NULL,
    ADD COLUMN investment_company VARCHAR(150) NULL,
    ADD COLUMN current_stage VARCHAR(100) NULL,
    ADD COLUMN pan_number VARCHAR(20) NULL,
    ADD COLUMN investment_amount DECIMAL(15, 2) NULL,
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL,
    ADD COLUMN location_captured_at DATETIME NULL,
    ADD COLUMN location_accuracy DOUBLE NULL,
    ADD COLUMN google_maps_url VARCHAR(512) NULL;
