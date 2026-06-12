-- ============================================================
-- Mudra — Milestone 3 initial schema
-- Run this once in the Supabase SQL Editor (or `supabase db push`).
-- Everything is protected by Row Level Security: a user can only
-- ever see and touch data belonging to a family they are part of.
-- ============================================================

create extension if not exists pgcrypto;

-- ---------- Enums ----------

create type public.app_role as enum ('senior', 'caregiver', 'admin');
create type public.member_role as enum ('owner', 'caregiver', 'viewer');
create type public.shortcut_type as enum ('phone_call', 'video_call', 'whatsapp_chat', 'gallery', 'help', 'custom_app');
create type public.media_type as enum ('image', 'video');
create type public.call_type as enum ('phone', 'video', 'whatsapp');
create type public.call_status as enum ('started', 'completed', 'missed', 'failed', 'cancelled');

-- ---------- Tables ----------

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null,
  role public.app_role not null default 'caregiver',
  avatar_path text,
  locale text not null default 'en',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.families (
  id uuid primary key default gen_random_uuid(),
  display_name text not null,
  created_by uuid not null references public.profiles(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.family_members (
  family_id uuid not null references public.families(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  role public.member_role not null default 'caregiver',
  created_at timestamptz not null default now(),
  primary key (family_id, user_id)
);

create table public.senior_profiles (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  display_name text not null,
  avatar_path text,
  preferred_language text not null default 'en',
  accessibility_config jsonb not null default '{"fontScale":"large","contrast":"high","voicePrompts":true,"hapticsEnabled":true}'::jsonb,
  emergency_config jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.senior_devices (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  senior_profile_id uuid not null references public.senior_profiles(id) on delete cascade,
  device_label text not null,
  android_id_hash text,
  app_version text,
  launcher_mode_enabled boolean not null default false,
  last_seen_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.trusted_contacts (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  senior_profile_id uuid not null references public.senior_profiles(id) on delete cascade,
  display_name text not null,
  relation_label text not null,
  phone_e164 text,
  whatsapp_e164 text,
  avatar_path text,
  can_phone_call boolean not null default true,
  can_video_call boolean not null default false,
  can_whatsapp boolean not null default false,
  emergency_priority int,
  sort_order int not null default 0,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint phone_or_whatsapp_required check (phone_e164 is not null or whatsapp_e164 is not null)
);

create table public.shortcut_tiles (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  senior_profile_id uuid not null references public.senior_profiles(id) on delete cascade,
  shortcut_type public.shortcut_type not null,
  label text not null,
  contact_id uuid references public.trusted_contacts(id) on delete set null,
  config jsonb not null default '{}'::jsonb,
  sort_order int not null default 0,
  enabled boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.gallery_items (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  senior_profile_id uuid not null references public.senior_profiles(id) on delete cascade,
  uploaded_by uuid references public.profiles(id) on delete set null,
  storage_path text not null,
  thumbnail_path text,
  caption text,
  media_type public.media_type not null default 'image',
  taken_at timestamptz,
  approved_at timestamptz default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.call_events (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  senior_profile_id uuid not null references public.senior_profiles(id) on delete cascade,
  contact_id uuid references public.trusted_contacts(id) on delete set null,
  call_type public.call_type not null,
  status public.call_status not null default 'started',
  started_at timestamptz not null default now(),
  ended_at timestamptz,
  failure_reason text,
  metadata jsonb not null default '{}'::jsonb
);

create table public.audit_events (
  id uuid primary key default gen_random_uuid(),
  family_id uuid references public.families(id) on delete cascade,
  actor_user_id uuid references public.profiles(id) on delete set null,
  subject_senior_profile_id uuid references public.senior_profiles(id) on delete set null,
  action text not null,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create table public.device_settings (
  senior_device_id uuid primary key references public.senior_devices(id) on delete cascade,
  settings jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);

-- ---------- updated_at maintenance ----------

create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end $$;

create trigger trg_profiles_updated before update on public.profiles
  for each row execute function public.set_updated_at();
create trigger trg_families_updated before update on public.families
  for each row execute function public.set_updated_at();
create trigger trg_senior_profiles_updated before update on public.senior_profiles
  for each row execute function public.set_updated_at();
create trigger trg_senior_devices_updated before update on public.senior_devices
  for each row execute function public.set_updated_at();
create trigger trg_trusted_contacts_updated before update on public.trusted_contacts
  for each row execute function public.set_updated_at();
create trigger trg_shortcut_tiles_updated before update on public.shortcut_tiles
  for each row execute function public.set_updated_at();
create trigger trg_gallery_items_updated before update on public.gallery_items
  for each row execute function public.set_updated_at();
create trigger trg_device_settings_updated before update on public.device_settings
  for each row execute function public.set_updated_at();

-- ---------- Auto-create a profile when a user signs up ----------

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name)
  values (
    new.id,
    coalesce(
      new.raw_user_meta_data->>'full_name',
      split_part(coalesce(new.email, ''), '@', 1),
      'Family member'
    )
  )
  on conflict (id) do nothing;
  return new;
end $$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------- Helper functions used by RLS ----------

create or replace function public.is_family_member(target_family_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.family_members fm
    where fm.family_id = target_family_id
      and fm.user_id = auth.uid()
  );
$$;

create or replace function public.is_family_admin(target_family_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.family_members fm
    where fm.family_id = target_family_id
      and fm.user_id = auth.uid()
      and fm.role in ('owner', 'caregiver')
  );
$$;

-- Creates a family and makes the caller its owner in one atomic step
-- (RLS makes the two separate inserts a chicken-and-egg problem otherwise).
create or replace function public.create_family(family_name text)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  new_family_id uuid;
begin
  if auth.uid() is null then
    raise exception 'not authenticated';
  end if;
  insert into public.families (display_name, created_by)
  values (family_name, auth.uid())
  returning id into new_family_id;
  insert into public.family_members (family_id, user_id, role)
  values (new_family_id, auth.uid(), 'owner');
  return new_family_id;
end $$;

grant execute on function public.create_family(text) to authenticated;

-- ---------- Row Level Security ----------

alter table public.profiles enable row level security;
alter table public.families enable row level security;
alter table public.family_members enable row level security;
alter table public.senior_profiles enable row level security;
alter table public.senior_devices enable row level security;
alter table public.trusted_contacts enable row level security;
alter table public.shortcut_tiles enable row level security;
alter table public.gallery_items enable row level security;
alter table public.call_events enable row level security;
alter table public.audit_events enable row level security;
alter table public.device_settings enable row level security;

-- Profiles: each user reads/updates only themselves.
create policy profiles_select_self on public.profiles
for select to authenticated
using (id = auth.uid());

create policy profiles_insert_self on public.profiles
for insert to authenticated
with check (id = auth.uid());

create policy profiles_update_self on public.profiles
for update to authenticated
using (id = auth.uid())
with check (id = auth.uid());

-- Families: members read; creation goes through create_family(); admins update.
create policy families_select_member on public.families
for select to authenticated
using (public.is_family_member(id));

create policy families_update_admin on public.families
for update to authenticated
using (public.is_family_admin(id))
with check (public.is_family_admin(id));

-- Family members: see your own memberships and your family's roster;
-- admins manage the roster; anyone may leave.
create policy family_members_select on public.family_members
for select to authenticated
using (user_id = auth.uid() or public.is_family_member(family_id));

create policy family_members_insert_admin on public.family_members
for insert to authenticated
with check (public.is_family_admin(family_id));

create policy family_members_update_admin on public.family_members
for update to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

create policy family_members_delete on public.family_members
for delete to authenticated
using (user_id = auth.uid() or public.is_family_admin(family_id));

-- Family-scoped reads for members.
create policy senior_profiles_select_family on public.senior_profiles
for select to authenticated
using (public.is_family_member(family_id));

create policy senior_devices_select_family on public.senior_devices
for select to authenticated
using (public.is_family_member(family_id));

create policy trusted_contacts_select_family on public.trusted_contacts
for select to authenticated
using (public.is_family_member(family_id));

create policy shortcut_tiles_select_family on public.shortcut_tiles
for select to authenticated
using (public.is_family_member(family_id));

create policy gallery_items_select_family on public.gallery_items
for select to authenticated
using (public.is_family_member(family_id));

create policy call_events_select_family on public.call_events
for select to authenticated
using (public.is_family_member(family_id));

create policy audit_events_select_family on public.audit_events
for select to authenticated
using (family_id is not null and public.is_family_member(family_id));

-- Admin/caregiver writes.
create policy senior_profiles_write_admin on public.senior_profiles
for all to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

create policy senior_devices_write_admin on public.senior_devices
for all to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

create policy trusted_contacts_write_admin on public.trusted_contacts
for all to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

create policy shortcut_tiles_write_admin on public.shortcut_tiles
for all to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

create policy gallery_items_write_admin on public.gallery_items
for all to authenticated
using (public.is_family_admin(family_id))
with check (public.is_family_admin(family_id));

-- Call + audit logs: any family member may append; nobody edits history.
create policy call_events_insert_member on public.call_events
for insert to authenticated
with check (public.is_family_member(family_id));

create policy audit_events_insert_member on public.audit_events
for insert to authenticated
with check (family_id is not null and public.is_family_member(family_id));

-- Device settings: scoped through the owning device's family.
create policy device_settings_select_family on public.device_settings
for select to authenticated
using (exists (
  select 1 from public.senior_devices sd
  where sd.id = senior_device_id and public.is_family_member(sd.family_id)
));

create policy device_settings_write_admin on public.device_settings
for all to authenticated
using (exists (
  select 1 from public.senior_devices sd
  where sd.id = senior_device_id and public.is_family_admin(sd.family_id)
))
with check (exists (
  select 1 from public.senior_devices sd
  where sd.id = senior_device_id and public.is_family_admin(sd.family_id)
));

-- ---------- Private storage bucket for family photos ----------
-- Object paths are family-scoped: <family_id>/<senior_profile_id>/<item_id>.<ext>
-- The first path segment is the family id, which the policies check.

insert into storage.buckets (id, name, public)
values ('family-gallery', 'family-gallery', false)
on conflict (id) do nothing;

create policy "family gallery read for members" on storage.objects
for select to authenticated
using (
  bucket_id = 'family-gallery'
  and public.is_family_member(((storage.foldername(name))[1])::uuid)
);

create policy "family gallery insert for admins" on storage.objects
for insert to authenticated
with check (
  bucket_id = 'family-gallery'
  and public.is_family_admin(((storage.foldername(name))[1])::uuid)
);

create policy "family gallery update for admins" on storage.objects
for update to authenticated
using (
  bucket_id = 'family-gallery'
  and public.is_family_admin(((storage.foldername(name))[1])::uuid)
);

create policy "family gallery delete for admins" on storage.objects
for delete to authenticated
using (
  bucket_id = 'family-gallery'
  and public.is_family_admin(((storage.foldername(name))[1])::uuid)
);
