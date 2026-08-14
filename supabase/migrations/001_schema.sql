-- =====================================================================
-- Silent Witness – schema v1
-- Tables store ONLY ciphertext + integrity hashes. All user content is
-- AES-256-GCM encrypted client-side before it ever reaches Postgres.
-- Run this in the Supabase SQL editor (or via the CLI: supabase db push).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Entries (evidence log)
-- ---------------------------------------------------------------------
create table if not exists public.entries (
    id             text primary key,
    user_id        uuid not null references auth.users (id) on delete cascade,
    encrypted_data text not null,
    iv             text not null,
    salt           text not null,
    hash           text not null,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz,
    photo_hash     text,
    audio_hash     text,
    is_deleted     boolean not null default false,
    sync_version   bigint not null default 1
);

create index if not exists entries_user_id_idx on public.entries (user_id);

alter table public.entries enable row level security;

drop policy if exists "entries_select_own" on public.entries;
create policy "entries_select_own" on public.entries
    for select using (auth.uid() = user_id);

drop policy if exists "entries_insert_own" on public.entries;
create policy "entries_insert_own" on public.entries
    for insert with check (auth.uid() = user_id);

drop policy if exists "entries_update_own" on public.entries;
create policy "entries_update_own" on public.entries
    for update using (auth.uid() = user_id);

drop policy if exists "entries_delete_own" on public.entries;
create policy "entries_delete_own" on public.entries
    for delete using (auth.uid() = user_id);

-- ---------------------------------------------------------------------
-- Contacts (trusted contacts, encrypted)
-- ---------------------------------------------------------------------
create table if not exists public.contacts (
    id             text primary key,
    user_id        uuid not null references auth.users (id) on delete cascade,
    encrypted_data text not null,
    iv             text not null,
    salt           text not null,
    hash           text not null,
    tier           int  not null default 1,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz
);

create index if not exists contacts_user_id_idx on public.contacts (user_id);

alter table public.contacts enable row level security;

drop policy if exists "contacts_select_own" on public.contacts;
create policy "contacts_select_own" on public.contacts
    for select using (auth.uid() = user_id);

drop policy if exists "contacts_insert_own" on public.contacts;
create policy "contacts_insert_own" on public.contacts
    for insert with check (auth.uid() = user_id);

drop policy if exists "contacts_update_own" on public.contacts;
create policy "contacts_update_own" on public.contacts
    for update using (auth.uid() = user_id);

drop policy if exists "contacts_delete_own" on public.contacts;
create policy "contacts_delete_own" on public.contacts
    for delete using (auth.uid() = user_id);

-- ---------------------------------------------------------------------
-- Safety plan (one per user, encrypted)
-- ---------------------------------------------------------------------
create table if not exists public.safety_plans (
    user_id        uuid primary key references auth.users (id) on delete cascade,
    encrypted_data text not null,
    iv             text not null,
    salt           text not null,
    hash           text not null,
    updated_at     timestamptz not null default now()
);

alter table public.safety_plans enable row level security;

drop policy if exists "safety_plans_select_own" on public.safety_plans;
create policy "safety_plans_select_own" on public.safety_plans
    for select using (auth.uid() = user_id);

drop policy if exists "safety_plans_insert_own" on public.safety_plans;
create policy "safety_plans_insert_own" on public.safety_plans
    for insert with check (auth.uid() = user_id);

drop policy if exists "safety_plans_update_own" on public.safety_plans;
create policy "safety_plans_update_own" on public.safety_plans
    for update using (auth.uid() = user_id);

drop policy if exists "safety_plans_delete_own" on public.safety_plans;
create policy "safety_plans_delete_own" on public.safety_plans
    for delete using (auth.uid() = user_id);

-- ---------------------------------------------------------------------
-- Check-in settings (one per user, NOT sensitive so stored in plaintext)
-- ---------------------------------------------------------------------
create table if not exists public.check_in_settings (
    user_id        uuid primary key references auth.users (id) on delete cascade,
    enabled        boolean not null default false,
    interval_hours int     not null default 4,
    notify_tier    int     not null default 1,
    updated_at     timestamptz not null default now()
);

alter table public.check_in_settings enable row level security;

drop policy if exists "check_in_settings_select_own" on public.check_in_settings;
create policy "check_in_settings_select_own" on public.check_in_settings
    for select using (auth.uid() = user_id);

drop policy if exists "check_in_settings_insert_own" on public.check_in_settings;
create policy "check_in_settings_insert_own" on public.check_in_settings
    for insert with check (auth.uid() = user_id);

drop policy if exists "check_in_settings_update_own" on public.check_in_settings;
create policy "check_in_settings_update_own" on public.check_in_settings
    for update using (auth.uid() = user_id);

drop policy if exists "check_in_settings_delete_own" on public.check_in_settings;
create policy "check_in_settings_delete_own" on public.check_in_settings
    for delete using (auth.uid() = user_id);

-- ---------------------------------------------------------------------
-- Grants for the anon (supabase anon key) and authenticated roles
-- ---------------------------------------------------------------------
grant select, insert, update, delete on public.entries           to anon, authenticated;
grant select, insert, update, delete on public.contacts          to anon, authenticated;
grant select, insert, update, delete on public.safety_plans      to anon, authenticated;
grant select, insert, update, delete on public.check_in_settings to anon, authenticated;

-- ---------------------------------------------------------------------
-- Private storage bucket for photo/audio evidence blobs
-- Files are uploaded under `<user_id>/...` and RLS keys access to the
-- owner's own folder. (Blob upload itself is a documented stub in the app.)
-- ---------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values ('evidence', 'evidence', false)
on conflict (id) do nothing;

drop policy if exists "evidence_owner_all" on storage.objects;
create policy "evidence_owner_all" on storage.objects
    for all
    using (
        bucket_id = 'evidence'
        and auth.uid()::text = (storage.foldername(name))[1]
    )
    with check (
        bucket_id = 'evidence'
        and auth.uid()::text = (storage.foldername(name))[1]
    );
