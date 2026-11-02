-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V55
-- Description: Add optional questions to client meeting update workflow:
--              residential address, office address, current city,
--              preferred contact time, occupation/business type,
--              company/organization name, communication preference,
--              preferred investment service, client requirements, other info notes.
-- ==============================================================================

ALTER TABLE client_meetings
    ADD COLUMN residential_address            VARCHAR(500)   NULL AFTER remarks,
    ADD COLUMN office_address                 VARCHAR(500)   NULL AFTER residential_address,
    ADD COLUMN current_city                   VARCHAR(100)   NULL AFTER office_address,
    ADD COLUMN preferred_contact_time         VARCHAR(100)   NULL AFTER current_city,
    ADD COLUMN occupation_business_type       VARCHAR(100)   NULL AFTER preferred_contact_time,
    ADD COLUMN company_organization_name      VARCHAR(150)   NULL AFTER occupation_business_type,
    ADD COLUMN communication_preference       VARCHAR(50)    NULL AFTER company_organization_name,
    ADD COLUMN preferred_investment_service   VARCHAR(150)   NULL AFTER communication_preference,
    ADD COLUMN client_requirements            VARCHAR(1000)  NULL AFTER preferred_investment_service,
    ADD COLUMN other_info_notes               VARCHAR(1000)  NULL AFTER client_requirements;

ALTER TABLE client_meeting_updates
    ADD COLUMN residential_address            VARCHAR(500)   NULL AFTER remarks,
    ADD COLUMN office_address                 VARCHAR(500)   NULL AFTER residential_address,
    ADD COLUMN current_city                   VARCHAR(100)   NULL AFTER office_address,
    ADD COLUMN preferred_contact_time         VARCHAR(100)   NULL AFTER current_city,
    ADD COLUMN occupation_business_type       VARCHAR(100)   NULL AFTER preferred_contact_time,
    ADD COLUMN company_organization_name      VARCHAR(150)   NULL AFTER occupation_business_type,
    ADD COLUMN communication_preference       VARCHAR(50)    NULL AFTER company_organization_name,
    ADD COLUMN preferred_investment_service   VARCHAR(150)   NULL AFTER communication_preference,
    ADD COLUMN client_requirements            VARCHAR(1000)  NULL AFTER preferred_investment_service,
    ADD COLUMN other_info_notes               VARCHAR(1000)  NULL AFTER client_requirements;

