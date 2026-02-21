-- Stripe intent references + local payout accounts (merged from V3 + V4)
alter table payment_intents
  add column if not exists provider_charge_reference text null;

alter table payment_intents
  add column if not exists provider_refund_reference text null;

alter table payment_intents
  add column if not exists provider_payment_method_reference text null;

alter table payment_intents
  add column if not exists provider_customer_reference text null;

create table if not exists local_payout_accounts (
  id uuid primary key,
  provider text not null,
  country text not null,
  account_name text not null,
  account_reference text not null,
  status text not null default 'ACTIVE',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_local_payout_provider_country unique (provider, country)
);

insert into local_payout_accounts (id, provider, country, account_name, account_reference, status)
values
  (gen_random_uuid(), 'ORANGE_MONEY', 'SN', 'Orange Money SN', 'OM_SN_DEFAULT', 'ACTIVE'),
  (gen_random_uuid(), 'WAVE', 'SN', 'Wave SN', 'WAVE_SN_DEFAULT', 'ACTIVE')
on conflict (provider, country) do nothing;
