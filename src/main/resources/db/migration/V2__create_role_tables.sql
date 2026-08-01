-- ==============================================================================
-- BlueAnt CRM ERP - Role & Permission Schema
-- Version: V2
-- Description: Core tables for Authorization and Access Control
-- ==============================================================================

create table permissions (
    id bigint not null auto_increment,
    display_order integer not null,
    is_deleted bit not null,
    system_permission bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    updated_at datetime(6),
    code varchar(100) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    module varchar(100) not null,
    name varchar(100) not null,
    updated_by varchar(100),
    description varchar(500),
    remarks varchar(500),
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table roles (
    id bigint not null auto_increment,
    default_role bit not null,
    display_order integer not null,
    is_deleted bit not null,
    system_role bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    updated_at datetime(6),
    code varchar(100) not null,
    created_by varchar(100),
    deleted_by varchar(100),
    name varchar(100) not null,
    updated_by varchar(100),
    description varchar(500),
    remarks varchar(500),
    status enum ('ACTIVE','ARCHIVED','INACTIVE','PENDING','SUSPENDED') not null,
    primary key (id)
) engine=InnoDB;

create table role_permissions (
    id bigint not null auto_increment,
    is_deleted bit not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    permission_id bigint not null,
    role_id bigint not null,
    updated_at datetime(6),
    created_by varchar(100),
    deleted_by varchar(100),
    updated_by varchar(100),
    primary key (id)
) engine=InnoDB;

create index idx_permission_name on permissions (name);
create index idx_permission_code on permissions (code);
create index idx_permission_module on permissions (module);
create index idx_permission_status on permissions (status);
alter table permissions add constraint uk_permission_code unique (code);

alter table roles add constraint uk_role_code unique (code);

alter table role_permissions add constraint FKegdk29eiy7mdtefy5c7eirr6e foreign key (permission_id) references permissions (id);
alter table role_permissions add constraint FKn5fotdgk8d1xvo8nav9uv3muc foreign key (role_id) references roles (id);
