-- ==============================================================================
-- V56: Role-Based Query Indexing Optimization
-- Database: MySQL 8.0+ (InnoDB)
-- Modules: Lead Management, Client & Family, Follow-ups, CRM Onboarding, Audit
-- Note: Work Follow tables (client_meetings, client_meeting_verifications,
--       client_meeting_updates, client_meeting_verification_business_items)
--       are STRICTLY EXCLUDED.
-- ==============================================================================

-- 1. Leads Table Optimizations (RM, SM, Leader, Dashboard, PC Coordinator)
-- Accelerates Dashboard status pipeline counts (replaces index_merge with covering index)
CREATE INDEX idx_leads_sales_person_status 
ON leads (assigned_sales_person_id, lead_status);

-- Accelerates Team Leader / Sales Manager lead pipeline counts
CREATE INDEX idx_leads_leader_status 
ON leads (assigned_leader_id, lead_status);

-- Eliminates Full Table Scans on system-wide date range reports
CREATE INDEX idx_leads_created_at 
ON leads (created_at);

-- Accelerates employee-scoped lead date range searches
CREATE INDEX idx_leads_sales_person_created_at 
ON leads (assigned_sales_person_id, created_at);

-- Accelerates PC Coordinator unassigned physical lead dispatch queue
CREATE INDEX idx_leads_physical_unassigned 
ON leads (is_physical_lead, assigned_sales_person_id);


-- 2. Clients Table Optimizations (RM, SC, CRM Onboarding, Admin, Super Admin)
-- Eliminates filesort on paginated client list by sales person and status
CREATE INDEX idx_clients_sales_person_status_del_id 
ON clients (sales_person_id, client_status, is_deleted, id);

-- Accelerates Relationship Manager scoped client retrieval
CREATE INDEX idx_clients_rm_deleted 
ON clients (relationship_manager_id, is_deleted);


-- 3. Client Follow-ups Table Optimizations (RM)
-- Eliminates filesort on chronological follow-up agenda lists
CREATE INDEX idx_cl_followup_sp_date 
ON client_followups (sales_person_id, followup_date DESC);


-- 4. Client Family Members Table Optimizations (CRM Onboarding, Admin)
-- Accelerates family membership status validation and eligible head searches
CREATE INDEX idx_cl_fammem_family_status 
ON client_family_members (family_id, membership_status);

-- Accelerates active client membership validation
CREATE INDEX idx_cl_fammem_client_status 
ON client_family_members (client_id, membership_status);


-- 5. Client Family Head History Table Optimizations (CRM Onboarding, Admin, PC Coordinator)
-- Eliminates filesort on family head history audit logs
CREATE INDEX idx_cl_famhist_family_changed_at 
ON client_family_head_histories (family_id, changed_at DESC, id DESC);


-- 6. Meetings Table Optimizations (Lead Meeting Workflow: PC Coordinator, SC)
-- Accelerates lead meeting sequence resolution and status checks
CREATE INDEX idx_meetings_lead_status_seq 
ON meetings (lead_id, meeting_status, meeting_sequence);


-- 7. CRM Verifications Table Optimizations (CRM Onboarding)
-- Accelerates lead verification status checks
CREATE INDEX idx_crm_verif_lead_status 
ON crm_verifications (lead_id, verification_status);


-- 8. Client Import Jobs Table Optimizations (CRM Onboarding, Admin)
-- Eliminates filesort on user import job history pagination
CREATE INDEX idx_client_import_created_by_date 
ON client_import_jobs (created_by, created_at DESC);

-- Accelerates heartbeat watchdog query for stalled import jobs
CREATE INDEX idx_client_import_status_heartbeat 
ON client_import_jobs (status, heartbeat_at);


-- 9. Audit Logs Table Optimizations (Admin, Super Admin)
-- Accelerates entity audit trail history retrieval over time
CREATE INDEX idx_audit_entity_time 
ON audit_logs (entity_name, entity_id, action_time DESC);
