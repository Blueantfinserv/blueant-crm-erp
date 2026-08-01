-- ==============================================================================
-- BlueAnt CRM ERP - Lead Schema
-- Version: V4
-- Description: Core tables for Sales and Lead Management
-- ==============================================================================

create table leads (
    id bigint not null auto_increment,
    version bigint not null,
    crm_handover bit,
    investment_amount decimal(19,2),
    is_deleted bit not null,
    leader_present bit,
    meeting_time time(6),
    service_request_required bit,
    assigned_leader_id bigint,
    assigned_sales_person_id bigint,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    last_call_date datetime(6),
    meeting_date datetime(6),
    next_plan_date datetime(6),
    updated_at datetime(6),
    alternate_number varchar(20),
    mobile_number varchar(20) not null,
    pan varchar(20),
    lead_code varchar(30) not null,
    unique_lead_id varchar(50) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    updated_by varchar(100),
    client_name varchar(150) not null,
    company_name varchar(150),
    email varchar(150),
    location varchar(150),
    meeting_with varchar(150),
    meeting_location varchar(200),
    meeting_remarks varchar(1000),
    remarks varchar(1000),
    discussion varchar(2000),
    duplicate_lead_status enum ('DUPLICATE','ELIGIBLE_FOR_TRANSFER','ORIGINAL','TRANSFERRED','TRANSFER_RESTRICTED','UNDER_VERIFICATION') not null,
    lead_source enum ('API','BRANCH','BUSINESS_PARTNER','CHANNEL_PARTNER','EMAIL_CAMPAIGN','EMPLOYEE_REFERRAL','EVENT','FACEBOOK','FIELD_VISIT','GOOGLE','IMPORT','INSTAGRAM','LINKEDIN','MANUAL','OTHER','REFERRAL','SEMINAR','SMS_CAMPAIGN','TELE_CALLING','WALK_IN','WEBSITE','WHATSAPP','YOUTUBE') not null,
    lead_stage enum ('CLIENT_ONBOARDED','COMPLETED','CRM_HANDOVER','DOCUMENT_COLLECTION','DUPLICATE_CHECK','FIRST_CONTACT','FOLLOW_UP','INTRO_MEETING','INTRO_MEETING_COMPLETED','INTRO_MEETING_SCHEDULED','INVESTMENT_CONFIRMED','LEAD_ASSIGNED','LEAD_CREATED','NEED_ANALYSIS','PC_VERIFICATION','PRODUCT_DISCUSSION','PROPOSAL_SHARED','SERVICE_REQUEST_CREATED') not null,
    lead_status enum ('ALREADY_CLIENT','ASSIGNED','CONTACTED','CONVERTED','DOCUMENT_PENDING','DUPLICATE','FOLLOW_UP_PENDING','LOST','MEETING_COMPLETED','MEETING_SCHEDULED','NEW','NOT_INTERESTED','ON_HOLD','REMOVED','TRANSFERRED','WORK_IN_PROGRESS') not null,
    lead_type enum ('AIF','BONDS','CHILD_EDUCATION','CORPORATE_FINANCE','FINANCIAL_PLANNING','FIXED_DEPOSIT','INSURANCE','LOAN','MUTUAL_FUND','NPS','OTHER','PMS','RETIREMENT_PLANNING','SHARE_MARKET','TAX_SAVING','WEALTH_MANAGEMENT') not null,
    meeting_mode enum ('PHYSICAL','TELEPHONIC','VIRTUAL'),
    priority enum ('CRITICAL','HIGH','LOW','MEDIUM','VIP') not null,
    product_type enum ('INSURANCE','LOAN','MUTUAL_FUND','SHARE'),
    profession enum ('BUSINESS_OWNER','CA','CONSULTANT','DOCTOR','ENGINEER','FARMER','FREELANCER','GOVERNMENT_EMPLOYEE','HOUSEWIFE','LAWYER','OTHER','PRIVATE_EMPLOYEE','RETIRED','SELF_EMPLOYED','STUDENT','TEACHER','TRADER'),
    primary key (id)
) engine=InnoDB;

create index idx_lead_mobile on leads (mobile_number);
create index idx_lead_status on leads (lead_status);
create index idx_lead_sales_person on leads (assigned_sales_person_id);
create index idx_lead_leader on leads (assigned_leader_id);
create index idx_lead_pan on leads (pan);
alter table leads add constraint uk_lead_code unique (lead_code);
alter table leads add constraint uk_lead_unique_id unique (unique_lead_id);
alter table leads add constraint fk_lead_leader foreign key (assigned_leader_id) references users (id);
alter table leads add constraint fk_lead_sales_person foreign key (assigned_sales_person_id) references users (id);
