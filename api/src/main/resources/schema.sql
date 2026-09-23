create extension if not exists pgcrypto;;

create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at = current_timestamp;
    return new;
end;
$$ language plpgsql;;

create table if not exists accounts (
    id uuid primary key default gen_random_uuid(),
    login_id varchar(50) not null unique,
    password varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);;

create or replace trigger update_accounts_updated_at
    before update on accounts
    for each row
    execute function set_updated_at();;

create table if not exists classrooms (
    id uuid primary key default gen_random_uuid(),
    name varchar(50) not null,
    classroom_number integer not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);;

create or replace trigger update_classrooms_updated_at
    before update on classrooms
    for each row
    execute function set_updated_at();;

create table if not exists managers (
    id uuid primary key default gen_random_uuid(),
    account_id uuid not null,
    classroom_id uuid not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (account_id) references accounts (id) on delete cascade on update cascade,
    foreign key (classroom_id) references classrooms (id) on update cascade
);;

create or replace trigger update_managers_updated_at
    before update on managers
    for each row
    execute function set_updated_at();;

create table if not exists tutors (
    id uuid primary key default gen_random_uuid(),
    account_id uuid not null,
    classroom_id uuid not null,
    tutor_number integer default null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    terminated boolean not null default false,
    termination_date date default null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (account_id) references accounts (id) on delete cascade on update cascade,
    foreign key (classroom_id) references classrooms (id) on update cascade
);;

create or replace trigger update_tutors_updated_at
    before update on tutors
    for each row
    execute function set_updated_at();;

create table if not exists salaries (
    id uuid primary key default gen_random_uuid(),
    tutor_id uuid not null,
    effective_date date not null,
    lesson_wage integer not null,
    office_wage integer not null,
    transportation_fee integer not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (tutor_id) references tutors (id) on delete cascade on update cascade,
    unique (tutor_id, effective_date)
);;

create or replace trigger update_salaries_updated_at
    before update on salaries
    for each row
    execute function set_updated_at();;

create table if not exists works (
    id uuid primary key default gen_random_uuid(),
    tutor_id uuid not null,
    classroom_id uuid not null,
    working_date date not null,
    transportation_fee integer not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (tutor_id) references tutors (id) on delete cascade on update cascade,
    foreign key (classroom_id) references classrooms (id) on update cascade,
    unique (tutor_id, working_date)
);;

create or replace trigger update_works_updated_at
    before update on works
    for each row
    execute function set_updated_at();;

create table if not exists lesson_work_details (
    id uuid primary key default gen_random_uuid(),
    work_id uuid not null,
    start_time time,
    end_time time,
    break_minutes integer,
    period_code_m boolean not null default false,
    period_code_k boolean not null default false,
    period_code_s boolean not null default false,
    period_code_a boolean not null default false,
    period_code_b boolean not null default false,
    period_code_c boolean not null default false,
    period_code_d boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (work_id) references works (id) on delete cascade on update cascade
);;

create or replace trigger update_lesson_work_details_updated_at
    before update on lesson_work_details
    for each row
    execute function set_updated_at();;

create table if not exists office_work_details (
    id uuid primary key default gen_random_uuid(),
    work_id uuid not null,
    start_time time,
    end_time time,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (work_id) references works (id) on delete cascade on update cascade
);;

create or replace trigger update_office_work_details_updated_at
    before update on office_work_details
    for each row
    execute function set_updated_at();;

create table if not exists other_work_details (
    id uuid primary key default gen_random_uuid(),
    work_id uuid not null,
    start_time time,
    end_time time,
    break_minutes integer,
    description varchar(500),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (work_id) references works (id) on delete cascade on update cascade
);;

create or replace trigger update_other_work_details_updated_at
    before update on other_work_details
    for each row
    execute function set_updated_at();;

create table if not exists templates (
    id uuid primary key default gen_random_uuid(),
    tutor_id uuid not null,
    classroom_id uuid not null,
    title varchar(100) not null,
    transportation_fee integer not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (tutor_id) references tutors (id) on delete cascade on update cascade,
    foreign key (classroom_id) references classrooms (id) on update cascade
);;

create or replace trigger update_templates_updated_at
    before update on templates
    for each row
    execute function set_updated_at();;

create table if not exists lesson_template_details (
    id uuid primary key default gen_random_uuid(),
    template_id uuid not null,
    start_time time,
    end_time time,
    break_minutes integer,
    period_code_m boolean not null default false,
    period_code_k boolean not null default false,
    period_code_s boolean not null default false,
    period_code_a boolean not null default false,
    period_code_b boolean not null default false,
    period_code_c boolean not null default false,
    period_code_d boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (template_id) references templates (id) on delete cascade on update cascade
);;

create or replace trigger update_lesson_template_details_updated_at
    before update on lesson_template_details
    for each row
    execute function set_updated_at();;

create table if not exists office_template_details (
    id uuid primary key default gen_random_uuid(),
    template_id uuid not null,
    start_time time,
    end_time time,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (template_id) references templates (id) on delete cascade on update cascade
);;

create or replace trigger update_office_template_details_updated_at
    before update on office_template_details
    for each row
    execute function set_updated_at();;

create table if not exists other_template_details (
    id uuid primary key default gen_random_uuid(),
    template_id uuid not null,
    start_time time,
    end_time time,
    break_minutes integer,
    description varchar(500),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (template_id) references templates (id) on delete cascade on update cascade
);;

create or replace trigger update_other_template_details_updated_at
    before update on other_template_details
    for each row
    execute function set_updated_at();;
