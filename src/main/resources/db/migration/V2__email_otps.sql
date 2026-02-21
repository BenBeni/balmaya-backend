-- V2__email_otps.sql

create table if not exists email_otps (
  id uuid primary key,
  email text not null,
  user_id text null,
  code text not null,
  expires_at timestamptz not null,
  created_at timestamptz not null default now(),
  last_sent_at timestamptz null,
  used_at timestamptz null
);
create index if not exists idx_email_otps_email on email_otps(email);
create index if not exists idx_email_otps_user on email_otps(user_id);
create index if not exists idx_email_otps_expires_at on email_otps(expires_at);
