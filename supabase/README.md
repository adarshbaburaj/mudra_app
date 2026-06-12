# Mudra — Supabase setup (Milestone 3)

## 1. Apply the schema (two copy-pastes, ~1 minute)

1. Open your project: https://supabase.com/dashboard → project `miimxktfpkgslcdtkzsr`.
2. Left sidebar → **SQL Editor** → **New query**.
3. Copy the entire contents of [`migrations/20260613000000_init.sql`](migrations/20260613000000_init.sql), paste, press **Run**.
4. You should see "Success. No rows returned".
5. Repeat with [`migrations/20260613001000_realtime.sql`](migrations/20260613001000_realtime.sql)
   — this turns on instant updates between family phones.

Optional but recommended for smoother onboarding: **Authentication →
Sign In / Providers → Email → turn OFF "Confirm email"**. Otherwise every new
account must click a link in their inbox before they can sign in (the app
explains this when it happens, so it works either way).

Verify it worked:
- **Table Editor** now lists: `profiles`, `families`, `family_members`, `senior_profiles`,
  `senior_devices`, `trusted_contacts`, `shortcut_tiles`, `gallery_items`,
  `call_events`, `audit_events`, `device_settings`.
- **Storage** now shows a **private** bucket called `family-gallery`.
- **Authentication → Policies** shows RLS enabled on every table.

> Re-running the file on the same project will error on `create type` — that's
> expected; it's a one-time migration.

## 2. Keys — what's safe and what's secret

- `sb_publishable_…` (the one in `local.properties`): safe to ship in the app.
  RLS is the real lock — the key only identifies the project.
- `service_role` key: **never** goes in the app, the repo, or chat. It bypasses
  RLS entirely. It's only for servers (Edge Functions, dashboards backend).

## 3. How the data model maps to Mudra

| Mudra concept | Table |
|---|---|
| The son/daughter's account | `profiles` (created automatically on signup) |
| One household | `families` (create via `select create_family('Our Family');`) |
| Who belongs to the household | `family_members` (`owner` / `caregiver` / `viewer`) |
| The parent using the simple screen | `senior_profiles` |
| The parent's phone | `senior_devices` + `device_settings` |
| People on the Call/WhatsApp screens | `trusted_contacts` |
| Home screen tile layout | `shortcut_tiles` |
| Family photo gallery | `gallery_items` + Storage bucket `family-gallery` |
| "Mom tried to call at 4pm" history | `call_events` |
| "Who changed what" history | `audit_events` |

Photo files live at `family-gallery/<family_id>/<senior_profile_id>/<item_id>.jpg`
— the first folder name is what the storage policies check.

## 4. Manual RLS tests (run in SQL Editor after creating 2 test users)

Create two users in **Authentication → Users → Add user** (e.g. a@test.com,
b@test.com), then in SQL Editor:

```sql
-- Pretend to be user A (paste their UID from the Users page):
select set_config('request.jwt.claims',
  json_build_object('sub', '<USER_A_UUID>', 'role', 'authenticated')::text, true);
set local role authenticated;

-- A creates a family:
select create_family('Family A');   -- note the returned uuid

-- A can see it:
select * from families;             -- 1 row ✓

-- Now pretend to be user B:
select set_config('request.jwt.claims',
  json_build_object('sub', '<USER_B_UUID>', 'role', 'authenticated')::text, true);

-- B must see NOTHING of family A:
select * from families;             -- 0 rows ✓
select * from trusted_contacts;     -- 0 rows ✓

-- B cannot write into family A (replace the uuid):
insert into senior_profiles (family_id, display_name)
values ('<FAMILY_A_UUID>', 'intruder');  -- must FAIL with RLS violation ✓
```

Run each block inside `begin; … rollback;` so the tests leave no trace.

## 5. What the Android app does with this (next step)

- Caregiver signs in (email magic link) → `profiles` row auto-created.
- Caregiver creates/joins a family → `create_family()` RPC.
- Contacts/photos added in Family Setup → written to Supabase AND cached locally.
- Senior's phone subscribes via Realtime → updates appear without touching it.
- Local DataStore stays as the offline cache, so the senior screens keep
  working with no internet.
