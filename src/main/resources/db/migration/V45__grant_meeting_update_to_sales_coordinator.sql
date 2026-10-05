-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V45
-- Description: Grant MEETING_UPDATE permission to SALES_COORDINATOR (role_id = 4)
-- ==============================================================================

INSERT INTO role_permissions (role_id, permission_id, is_deleted, created_at, updated_at)
SELECT r.id, p.id, 0, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code = 'MEETING_UPDATE'
WHERE r.id = 4
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
