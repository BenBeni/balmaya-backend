-- V1__init.sql
-- Versioned schema (immutable row versioning)

create table if not exists beneficiaries (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  user_id text not null,
  full_name text not null,
  relationship text null,
  country text not null default 'SN',
  provider text not null,
  phone_e164 text not null,
  status text not null default 'ACTIVE',

  constraint uq_benef_logical_version unique (logical_id, version)
);
create unique index if not exists uq_benef_current on beneficiaries(logical_id) where is_current = true;
create index if not exists idx_benef_user_current on beneficiaries(user_id) where is_current = true;

create table if not exists schedules (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  user_id text not null,
  beneficiary_id uuid not null references beneficiaries(id),
  status text not null,
  frequency text not null,
  timezone text not null,
  start_at timestamptz not null,
  weekly_day int null,
  monthly_day int null,
  next_run_at timestamptz not null,
  net_amount bigint not null default 0,
  currency text not null default 'EUR',
  funding_payment_method_ref text null,

  constraint uq_schedule_logical_version unique (logical_id, version)
);
create unique index if not exists uq_schedule_current on schedules(logical_id) where is_current = true;
create index if not exists idx_schedule_due on schedules(next_run_at) where is_current=true and status='ACTIVE';

create table if not exists schedule_items (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  schedule_id uuid not null references schedules(id) on delete cascade,
  category text not null,
  amount bigint not null,
  currency text not null,
  method text not null,
  biller_code text null,
  biller_account_ref text null,

  constraint uq_schedule_item_logical_version unique (logical_id, version)
);
create unique index if not exists uq_schedule_item_current on schedule_items(logical_id) where is_current = true;
create index if not exists idx_schedule_items_schedule on schedule_items(schedule_id) where is_current = true;

create table if not exists payments (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  user_id text not null,
  beneficiary_id uuid not null references beneficiaries(id),
  kind text not null,
  schedule_id uuid null references schedules(id),
  execute_at timestamptz not null,

  net_amount bigint not null,
  fee_amount bigint not null default 0,
  gross_amount bigint not null,
  currency text not null default 'EUR',
  fx_rate numeric(18,8) null,
  note text null,
  status text not null,
  failure_code text null,

  constraint uq_payment_logical_version unique (logical_id, version),
  constraint chk_payment_amounts check (gross_amount = net_amount + fee_amount)
);
create unique index if not exists uq_payment_current on payments(logical_id) where is_current = true;
create index if not exists idx_payments_user on payments(user_id) where is_current = true;

create table if not exists payment_items (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  payment_id uuid not null references payments(id) on delete cascade,
  category text not null,
  amount bigint not null,
  currency text not null,
  method text not null,
  biller_code text null,
  biller_account_ref text null,
  status text not null,
  failure_code text null,

  constraint uq_payment_item_logical_version unique (logical_id, version)
);
create unique index if not exists uq_payment_item_current on payment_items(logical_id) where is_current = true;
create index if not exists idx_payment_items_payment on payment_items(payment_id) where is_current=true;

create table if not exists payment_fee_components (
  id uuid primary key,
  logical_id uuid not null,
  version int not null,
  is_current boolean not null,
  created_at timestamptz not null default now(),
  created_by text not null,
  change_reason text null,

  payment_id uuid not null references payments(id) on delete cascade,
  code text not null,
  description text null,
  amount bigint not null,
  currency text not null,
  direction text not null,
  is_refundable boolean not null default false,
  provider text null,
  provider_reference text null,

  constraint uq_fee_logical_version unique (logical_id, version)
);
create unique index if not exists uq_fee_current on payment_fee_components(logical_id) where is_current = true;
create index if not exists idx_fee_payment on payment_fee_components(payment_id) where is_current=true;

create table if not exists payment_intents (
  id uuid primary key,
  payment_id uuid not null references payments(id) on delete cascade,
  provider text not null,
  provider_reference text not null,
  amount bigint not null,
  currency text not null,
  status text not null,
  created_at timestamptz not null default now()
);

create table if not exists executions (
  id uuid primary key,
  payment_item_id uuid not null references payment_items(id) on delete cascade,
  provider text not null,
  provider_reference text null,
  destination text not null,
  amount bigint not null,
  currency text not null,
  status text not null,
  failure_code text null,
  created_at timestamptz not null default now()
);

create table if not exists audit_log (
  id uuid primary key,
  actor_type text not null,
  actor_id text null,
  action text not null,
  target_type text not null,
  target_id text not null,
  metadata jsonb null,
  created_at timestamptz not null default now()
);
