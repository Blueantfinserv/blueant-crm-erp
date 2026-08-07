-- Redesign Meeting Update Workflow Migration

-- meetings: change meeting_conducted to VARCHAR enum holding CONDUCTED/NOT_CONDUCTED
ALTER TABLE meetings MODIFY COLUMN meeting_conducted VARCHAR(30);
UPDATE meetings SET meeting_conducted = CASE WHEN meeting_conducted = '1' OR meeting_conducted = 'true' THEN 'CONDUCTED' ELSE 'NOT_CONDUCTED' END;
ALTER TABLE meetings MODIFY COLUMN meeting_conducted VARCHAR(30) NOT NULL DEFAULT 'NOT_CONDUCTED';

-- meeting_updates: change meeting_conducted to VARCHAR enum
ALTER TABLE meeting_updates MODIFY COLUMN meeting_conducted VARCHAR(30);
UPDATE meeting_updates SET meeting_conducted = CASE WHEN meeting_conducted = '1' OR meeting_conducted = 'true' THEN 'CONDUCTED' ELSE 'NOT_CONDUCTED' END;
ALTER TABLE meeting_updates MODIFY COLUMN meeting_conducted VARCHAR(30) NOT NULL DEFAULT 'NOT_CONDUCTED';

-- meetings: add lead_status column (as MeetingLeadStatus enum)
ALTER TABLE meetings ADD COLUMN lead_status VARCHAR(50);

-- meetings: add google_maps_url column
ALTER TABLE meetings ADD COLUMN google_maps_url VARCHAR(512);

-- meeting_updates: add google_maps_url column
ALTER TABLE meeting_updates ADD COLUMN google_maps_url VARCHAR(512);
