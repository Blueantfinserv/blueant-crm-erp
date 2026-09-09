-- ============================================================================
-- Migration: V37__enrich_meeting_verifications_with_snapshot.sql
-- Description: Add complete meeting, lead, salesperson, and location snapshot
--              columns to meeting_verifications for audit and reporting.
--              Idempotent and safe against partially-applied migrations.
-- ============================================================================

DROP PROCEDURE IF EXISTS upgrade_v37_meeting_verifications;

DELIMITER //

CREATE PROCEDURE upgrade_v37_meeting_verifications()
BEGIN
    -- 1. Snapshot Columns on meeting_verifications
    -- Check if columns exist before adding them (idempotent for production recovery)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE table_schema = DATABASE() 
          AND table_name = 'meeting_verifications' 
          AND column_name = 'meeting_code'
    ) THEN
        ALTER TABLE meeting_verifications
            -- Meeting Identity
            ADD COLUMN meeting_code             VARCHAR(50)     NULL,
            ADD COLUMN meeting_number           INT             NULL,
            ADD COLUMN meeting_type             VARCHAR(30)     NULL,
            ADD COLUMN meeting_title            VARCHAR(100)    NULL,

            -- Lead / Client Snapshot
            ADD COLUMN lead_id                  BIGINT          NULL,
            ADD COLUMN lead_code                VARCHAR(30)     NULL,
            ADD COLUMN client_name              VARCHAR(150)    NULL,
            ADD COLUMN mobile_number            VARCHAR(20)     NULL,

            -- Sales Person Snapshot
            ADD COLUMN assigned_employee_id     BIGINT          NULL,
            ADD COLUMN employee_code            VARCHAR(30)     NULL,
            ADD COLUMN employee_name            VARCHAR(200)    NULL,

            -- Meeting Execution Snapshot
            ADD COLUMN meeting_date             DATE            NULL,
            ADD COLUMN meeting_time             TIME            NULL,
            ADD COLUMN meeting_mode             VARCHAR(30)     NULL,
            ADD COLUMN meeting_location         VARCHAR(255)    NULL,
            ADD COLUMN meeting_status           VARCHAR(30)     NULL,
            ADD COLUMN status                   VARCHAR(20)     NULL,
            ADD COLUMN meeting_remarks          VARCHAR(1000)   NULL,
            ADD COLUMN next_meeting_date        DATE            NULL,
            ADD COLUMN next_meeting_time        TIME            NULL,
            ADD COLUMN meeting_conducted        VARCHAR(30)     NULL,
            ADD COLUMN lead_status              VARCHAR(50)     NULL,

            -- Captured Meeting GPS & Visiting Card Data
            ADD COLUMN latitude                 DECIMAL(10, 7)  NULL,
            ADD COLUMN longitude                DECIMAL(10, 7)  NULL,
            ADD COLUMN location_accuracy        DOUBLE          NULL,
            ADD COLUMN location_captured_at     DATETIME        NULL,
            ADD COLUMN google_maps_url          VARCHAR(512)    NULL,
            ADD COLUMN visiting_card            VARCHAR(500)    NULL;
    END IF;

    -- 2. Indexes for reporting queries (added only if not present)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS 
        WHERE table_schema = DATABASE() 
          AND table_name = 'meeting_verifications' 
          AND index_name = 'idx_mv_meeting_code'
    ) THEN
        CREATE INDEX idx_mv_meeting_code ON meeting_verifications (meeting_code);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS 
        WHERE table_schema = DATABASE() 
          AND table_name = 'meeting_verifications' 
          AND index_name = 'idx_mv_lead_code'
    ) THEN
        CREATE INDEX idx_mv_lead_code ON meeting_verifications (lead_code);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS 
        WHERE table_schema = DATABASE() 
          AND table_name = 'meeting_verifications' 
          AND index_name = 'idx_mv_employee_code'
    ) THEN
        CREATE INDEX idx_mv_employee_code ON meeting_verifications (employee_code);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS 
        WHERE table_schema = DATABASE() 
          AND table_name = 'meeting_verifications' 
          AND index_name = 'idx_mv_verified_at'
    ) THEN
        CREATE INDEX idx_mv_verified_at ON meeting_verifications (verified_at);
    END IF;

    -- 3. Safe deterministic backfill for historical VERIFIED records
    -- NOTE: Table meetings uses column meeting_sequence (not meeting_number)
    UPDATE meeting_verifications mv
    JOIN meetings m ON mv.meeting_id = m.id
    LEFT JOIN leads l ON m.lead_id = l.id
    LEFT JOIN users u ON m.assigned_employee_id = u.id
    SET 
        mv.meeting_code = m.meeting_code,
        mv.meeting_number = m.meeting_sequence,
        mv.meeting_type = m.meeting_type,
        mv.meeting_title = m.meeting_title,
        mv.lead_id = l.id,
        mv.lead_code = l.lead_code,
        mv.client_name = l.client_name,
        mv.mobile_number = l.mobile_number,
        mv.assigned_employee_id = u.id,
        mv.employee_code = u.employee_code,
        mv.employee_name = TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))),
        mv.meeting_date = m.meeting_date,
        mv.meeting_time = m.meeting_time,
        mv.meeting_mode = m.meeting_mode,
        mv.meeting_location = COALESCE(m.meeting_location, l.location),
        mv.meeting_status = m.meeting_status,
        mv.status = m.status,
        mv.meeting_remarks = m.remarks,
        mv.next_meeting_date = m.next_meeting_date,
        mv.next_meeting_time = m.next_meeting_time,
        mv.meeting_conducted = m.meeting_conducted,
        mv.lead_status = m.lead_status,
        mv.latitude = m.latitude,
        mv.longitude = m.longitude,
        mv.location_accuracy = m.location_accuracy,
        mv.location_captured_at = m.location_captured_at,
        mv.google_maps_url = m.google_maps_url,
        mv.visiting_card = m.visiting_card
    WHERE mv.verification_status = 'VERIFIED';

END //

DELIMITER ;

CALL upgrade_v37_meeting_verifications();

DROP PROCEDURE IF EXISTS upgrade_v37_meeting_verifications;

