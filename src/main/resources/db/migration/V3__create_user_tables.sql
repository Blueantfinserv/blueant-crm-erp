-- ==============================================================================
-- BlueAnt CRM ERP - User & Organization Schema
-- Version: V3
-- Description: Core tables for Users, Departments, Teams, and Auth Tokens
-- ==============================================================================

create table departments (
    id bigint not null auto_increment,
    display_order integer not null,
    is_deleted bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    updated_at datetime(6),
    code varchar(50) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    name varchar(100) not null,
    updated_by varchar(100),
    description varchar(500),
    remarks varchar(500),
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table designations (
    id bigint not null auto_increment,
    display_order integer not null,
    hierarchy_level integer not null,
    is_deleted bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    department_id bigint not null,
    updated_at datetime(6),
    code varchar(50) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    name varchar(100) not null,
    updated_by varchar(100),
    description varchar(500),
    remarks varchar(500),
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table teams (
    id bigint not null auto_increment,
    display_order integer,
    is_deleted bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    department_id bigint not null,
    updated_at datetime(6),
    team_code varchar(30) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    team_name varchar(100) not null,
    updated_by varchar(100),
    description varchar(500),
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table users (
    id bigint not null auto_increment,
    account_enabled bit not null,
    account_locked bit not null,
    account_non_expired bit not null,
    credentials_non_expired bit not null,
    date_of_birth date,
    email_verified bit not null,
    failed_login_attempts integer not null,
    first_login bit not null,
    is_deleted bit not null,
    joining_date date,
    mobile_verified bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    department_id bigint not null,
    designation_id bigint not null,
    last_login_at datetime(6),
    last_logout_at datetime(6),
    password_changed_at datetime(6),
    password_expiry_at datetime(6),
    password_reset_at datetime(6),
    reporting_manager_id bigint,
    role_id bigint not null,
    team_id bigint not null,
    updated_at datetime(6),
    mobile_number varchar(20) not null,
    employee_code varchar(30) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    first_name varchar(100) not null,
    last_name varchar(100),
    updated_by varchar(100),
    email varchar(150) not null,
    remarks varchar(500),
    profile_image varchar(1000),
    last_login_device varchar(255),
    last_login_ip varchar(255),
    password varchar(255) not null,
    gender enum ('FEMALE','MALE','OTHER') not null,
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table auth_refresh_tokens (
    id bigint not null auto_increment,
    revoked bit not null,
    created_at datetime(6) not null,
    expiry_date datetime(6) not null,
    last_activity_at datetime(6),
    revoked_at datetime(6),
    updated_at datetime(6),
    user_id bigint not null,
    device_type varchar(50),
    browser varchar(100),
    created_by varchar(100),
    ip_address varchar(100),
    operating_system varchar(100),
    revoked_by varchar(100),
    session_id varchar(100) not null comment 'Unique session identifier',
    updated_by varchar(100),
    device_name varchar(150),
    refresh_token varchar(512) not null comment 'JWT Refresh Token',
    device_id varchar(255),
    location varchar(255),
    primary key (id)
) engine=InnoDB;

create index idx_department_name on departments (name);
create index idx_department_code on departments (code);
create index idx_department_status on departments (status);
alter table departments add constraint uk_department_name unique (name);
alter table departments add constraint uk_department_code unique (code);

create index idx_designation_name on designations (name);
create index idx_designation_code on designations (code);
create index idx_designation_status on designations (status);
create index idx_designation_level on designations (hierarchy_level);
alter table designations add constraint uk_designation_name unique (name);
alter table designations add constraint uk_designation_code unique (code);
alter table designations add constraint FK5kgb0o61xcwqr3scbtopsca3p foreign key (department_id) references departments (id);

create index idx_team_code on teams (team_code);
create index idx_team_name on teams (team_name);
create index idx_team_status on teams (status);
create index idx_team_department on teams (department_id);
create index idx_team_display_order on teams (display_order);
alter table teams add constraint uk_team_code unique (team_code);
alter table teams add constraint uk_team_name unique (team_name);
alter table teams add constraint fk_team_department foreign key (department_id) references departments (id);

create index idx_user_employee_code on users (employee_code);
create index idx_user_email on users (email);
create index idx_user_mobile on users (mobile_number);
create index idx_user_status on users (status);
create index idx_user_account_enabled on users (account_enabled);
create index idx_user_account_locked on users (account_locked);
create index idx_user_reporting_manager on users (reporting_manager_id);
create index idx_user_role on users (role_id);
create index idx_user_department on users (department_id);
create index idx_user_designation on users (designation_id);
create index idx_user_team on users (team_id);
alter table users add constraint uk_user_employee_code unique (employee_code);
alter table users add constraint uk_user_email unique (email);
alter table users add constraint uk_user_mobile unique (mobile_number);
alter table users add constraint fk_user_department foreign key (department_id) references departments (id);
alter table users add constraint fk_user_designation foreign key (designation_id) references designations (id);
alter table users add constraint fk_user_reporting_manager foreign key (reporting_manager_id) references users (id);
alter table users add constraint fk_user_role foreign key (role_id) references roles (id);
alter table users add constraint fk_user_team foreign key (team_id) references teams (id);

create index idx_refresh_token on auth_refresh_tokens (refresh_token);
create index idx_session_id on auth_refresh_tokens (session_id);
create index idx_user on auth_refresh_tokens (user_id);
create index idx_expiry on auth_refresh_tokens (expiry_date);
alter table auth_refresh_tokens add constraint UKt1ks8ganx84ypfyp0vrd215fo unique (session_id);
alter table auth_refresh_tokens add constraint UK7gjtumn4bd5gyvvl0t7wjnmib unique (refresh_token);
alter table auth_refresh_tokens add constraint fk_refresh_token_user foreign key (user_id) references users (id);
