-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V36
-- Description: Seed PHYSICAL_LEAD_ASSIGN permission and map to roles
-- ==============================================================================

-- 1. Insert PHYSICAL_LEAD_ASSIGN permission if missing
INSERT INTO permissions (code, name, module, status, display_order, description, remarks, is_deleted, system_permission, created_at, updated_at)
SELECT 'PHYSICAL_LEAD_ASSIGN', 'Assign Physical Lead', 'LEAD', 'ACTIVE', 1, 'Assign Physical Lead Permission', 'System Bootstrapped', 0, 1, NOW(), NOW()
FROM (SELECT 1) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'PHYSICAL_LEAD_ASSIGN');

-- 2. Map PHYSICAL_LEAD_ASSIGN permission to SALES_COORDINATOR, ADMIN, and SUPER_ADMIN roles if missing
INSERT INTO role_permissions (role_id, permission_id, is_deleted, created_at, updated_at)
SELECT r.id, p.id, 0, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code = 'PHYSICAL_LEAD_ASSIGN'
WHERE r.code IN ('SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp 
      WHERE rp.role_id = r.id AND rp.permission_id = p.id AND rp.is_deleted = 0
  );
