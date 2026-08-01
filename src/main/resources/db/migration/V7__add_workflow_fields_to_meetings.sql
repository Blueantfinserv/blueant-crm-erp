-- ============================================================================
-- Migration: Add Workflow Fields to Meetings Table
-- ============================================================================

ALTER TABLE meetings 
-- TODO: Future normalization should replace this comma-separated storage
ADD COLUMN company_participants VARCHAR(500),
ADD COLUMN client_participants VARCHAR(500),
ADD COLUMN address VARCHAR(500),
ADD COLUMN landmark VARCHAR(255),
ADD COLUMN google_location VARCHAR(255),
ADD COLUMN discussion VARCHAR(2000),
ADD COLUMN client_interest_level VARCHAR(50),
ADD COLUMN estimated_investment_amount DECIMAL(15,2),
ADD COLUMN expected_closing_date DATE;
