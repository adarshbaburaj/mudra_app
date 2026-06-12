-- ============================================================
-- Mudra — enable realtime change notifications
-- Run this AFTER 20260613000000_init.sql, in the same SQL Editor.
-- This lets the senior's phone update instantly when a caregiver
-- changes contacts or photos from another device.
-- ============================================================

alter publication supabase_realtime add table public.trusted_contacts;
alter publication supabase_realtime add table public.gallery_items;
alter publication supabase_realtime add table public.senior_profiles;
