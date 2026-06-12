# CLAUDE.md - ADA Senior Smartphone Assistant App

Owner: ADA
Working product name: OneTap Family (confirm with ADA before code)
Primary platform assumption: Android-first native app
Backend: Supabase
Last updated: 2026-06-12

IMPORTANT COMMUNICATION RULE
Every assistant response to ADA must start exactly with:

Hey ADA,

Then use this structure:

Hey ADA, this is the work done:
- Done: ...
- Need from you: ...
- Risks or tradeoffs: ...
- Next step I will take after your answer: ...

Do not start by saying "Hello World". Do not create a Hello World project until the "Before Hello World" questions below are answered or ADA explicitly says to proceed with defaults.

---

## 1. Product intent

Build an extremely simple smartphone experience for older adults who may have low vision, hand tremors, low confidence with smartphones, memory challenges, fear of making mistakes, or limited digital literacy.

The app must reduce the number of taps and decisions needed for these goals:

1. Video call loved ones.
2. Make a phone call to a trusted number without thinking much.
3. View family photos safely, especially photos that relatives may otherwise send through WhatsApp.
4. Open WhatsApp only for trusted contacts, reducing the chance of accidental stories, unwanted calls, messages to the wrong people, or navigation confusion.

The design principle is not "more features". The design principle is "less fear, fewer choices, bigger actions, safe recovery".

---

## 2. Research-backed design principles

Use these as non-negotiable product rules.

### 2.1 Older adult accessibility is mainstream accessibility

W3C WAI says designing products that are easier for older people is similar to designing for people with disabilities, and that WCAG guidance addresses many older-user needs.
Source: https://www.w3.org/WAI/older-users/

### 2.2 Touch targets must be much larger than normal apps

WCAG 2.2 target size minimum is 24 x 24 CSS px, but Android Accessibility guidance recommends touch targets at least 48dp x 48dp.
For this product, use a stricter senior-first standard:

- Minimum tappable target: 64dp x 64dp.
- Preferred primary action target: 88dp to 120dp high.
- Spacing between tappable items: at least 12dp; preferred 16dp to 24dp.
- Never place destructive actions near primary actions.
- No icon-only controls for senior-facing screens.

Sources:
- WCAG 2.2 Target Size: https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html
- Android touch target guidance: https://support.google.com/accessibility/android/answer/7101858

### 2.3 Simplify navigation and increase size/distance between controls

Systematic reviews on mobile apps for older adults identify simplified navigation, enlarged text/touch targets, voice interaction, and error-tolerant interfaces as key design elements. A JMIR systematic review found "Simplify" and "Increase the size and distance between interactive controls" among the most significant guidelines.
Sources:
- Springer 2025 systematic review: https://link.springer.com/article/10.1007/s40520-025-03157-7
- JMIR 2023 systematic review: https://mhealth.jmir.org/2023/1/e43186

### 2.4 Avoid abstract symbols and information overload

Recent research on smartphone adoption among older adults reports that abstract symbols, missing guidance, and information overload hinder understanding and engagement. Therefore:

- Every icon must have a text label.
- Use real contact photos where possible.
- Use relation labels, such as "Son", "Daughter", "Doctor", "Neighbor".
- Show one primary decision per screen.
- Use plain language.
- Avoid jargon such as "sync", "permissions", "deep link", "database", or "OTP" on senior-facing screens.

Source: https://www.frontiersin.org/journals/communication/articles/10.3389/fcomm.2026.1808537/full

### 2.5 Readability and error tolerance are critical

Nielsen Norman Group reports that older users often struggle with small text, poor contrast, tiny targets, and unforgiving interfaces. Therefore:

- Minimum senior-facing body text: 22sp.
- Preferred body text: 24sp.
- Primary button text: 26sp to 32sp.
- Use high contrast; aim for AAA contrast where practical.
- Do not rely on color alone.
- Error messages must be plain, visible, persistent, and actionable.
- Avoid timers that disappear quickly.

Source: https://www.nngroup.com/articles/usability-for-senior-citizens/

---

## 3. Core feasibility constraints

### 3.1 Supabase is not a video calling media server

Supabase is excellent for:

- Auth
- Families and trusted contacts
- Device pairing
- App settings
- Family photo gallery metadata
- Private file storage
- Audit logs
- Realtime updates
- Edge Functions for server-side logic

Supabase is not enough by itself for low-latency video calling. For video calls, ADA must choose one of:

1. Daily.co SDK
2. Agora SDK
3. Twilio Video
4. Jitsi Meet SDK / self-hosted Jitsi
5. Open WhatsApp chat only, with no guaranteed direct video-call API
6. Google Meet link launch
7. Custom WebRTC with a TURN/STUN/SFU provider

Do not build video calling before ADA chooses the video strategy. If ADA says "choose for MVP", use Jitsi Meet for fastest prototype or Daily.co for product-grade SDK/token flow, then document the tradeoff.

### 3.2 WhatsApp cannot be made read-only by our app

A normal third-party Android app cannot safely transform the official WhatsApp app into a read-only mode, hide its Story/Status features, or guarantee that the senior cannot accidentally call/message the wrong person once WhatsApp is open.

Allowed WhatsApp behavior for this product:

- Use official WhatsApp click-to-chat links for trusted contacts.
- Open only preconfigured trusted contact chats.
- Show a confirmation screen before leaving our safe app.
- Provide a fallback if WhatsApp is missing.
- Do not scrape WhatsApp databases.
- Do not use unofficial WhatsApp automation APIs.
- Do not use Android AccessibilityService to control WhatsApp unless ADA explicitly understands the Play Store, privacy, reliability, and consent risks.

Official WhatsApp Click to Chat help center:
https://faq.whatsapp.com/5913398998672934

### 3.3 Family photo viewing should be inside our app

To satisfy "view WhatsApp photos without messing up", create a safe in-app "Family Photos" gallery using Supabase Storage. Family members upload photos from a caregiver/admin screen or share photos into this app. The senior only sees:

- Big photo cards
- Big next/previous controls
- Optional caption read aloud
- Big back/home button

The senior must not see:

- Upload button
- Share button by default
- Delete button
- Story/status button
- Forward button
- Contact picker

### 3.4 Direct phone calling needs Android permission

For phone calls, Android supports:

- Safe dial mode: ACTION_DIAL with tel: URI. It opens the dialer with the number; user taps call. This usually does not need CALL_PHONE permission.
- Direct call mode: ACTION_CALL with tel: URI. It places the call directly and requires CALL_PHONE runtime permission. Android documentation says most apps should use ACTION_DIAL, and CALL_PHONE must be granted for ACTION_CALL.

Source: https://developer.android.com/reference/android/content/Intent

Product recommendation:

- Default MVP: safe dial mode with a giant "Call now" confirm screen.
- Optional ADA-approved mode: direct call for trusted contacts only, after caregiver grants CALL_PHONE permission.
- Emergency numbers: use ACTION_DIAL, not ACTION_CALL.

### 3.5 Android launcher / kiosk limits

A consumer app can implement a home-screen-like launcher and can be selected by the user/caregiver as the default Home app, but it cannot silently force itself to become the default launcher. In enterprise/dedicated-device scenarios, Android Enterprise Device Policy Controller features can set persistent preferred activities and lock task mode.

Use two modes:

1. Normal app mode: app opens like a regular app; caregivers can pin it or set it as default launcher manually.
2. Launcher mode: manifest includes HOME and DEFAULT categories; caregiver chooses it as default home app.
3. Managed/kiosk mode: only if ADA controls devices through MDM/Android Enterprise.

Source: https://developer.android.com/work/dpc/dedicated-devices/cookbook

---

## 4. Before Hello World - required questions for ADA

Before creating the initial project or writing any code, ask ADA these questions. If ADA says "use defaults", use the default marked below.

1. Platform: Android-only for MVP? Default: Android-only native Kotlin.
2. App name: what should the user-visible app name be? Default: OneTap Family.
3. Package name: confirm package, e.g. com.ada.onetapfamily. Default: com.ada.onetapfamily.
4. Distribution: Play Store, sideload APK, private family install, or managed device? Default: sideload/dev build first.
5. Video call strategy: Daily, Agora, Twilio, Jitsi, Google Meet, WhatsApp open-chat only, or decide later? Default: Jitsi for prototype, Daily for product-grade if ADA has budget.
6. Calling mode: safe dial mode or direct call mode? Default: safe dial mode.
7. WhatsApp scope: open trusted chat only, or no WhatsApp in MVP? Default: trusted chat shortcut only.
8. Family gallery: should photos be uploaded by caregivers in the app, a web dashboard, or both? Default: in-app caregiver mode first.
9. Languages: English only, or add local languages? Default: English, with i18n ready.
10. Senior users: one senior device or multiple seniors per family? Default: multiple seniors supported by schema.
11. Auth method for caregivers: email magic link, phone OTP, Google login, or admin-created accounts? Default: email magic link.
12. Supabase credentials: provide SUPABASE_URL and SUPABASE_ANON_KEY. Never ask ADA to put service role key in mobile code.
13. Region/privacy: any GDPR, HIPAA, UAE, India, or family privacy requirements? Default: privacy-first, no health records, no recordings.
14. Emergency button: include in MVP? What number should it call? Default: include disabled until configured.
15. Should the app hide all other apps by becoming a launcher? Default: optional launcher mode in MVP, not forced.
16. Should the app include caregiver remote management? Default: simple Supabase-backed caregiver mode; web dashboard later.
17. Target devices and Android versions: list phone models if known. Default: minSdk 26, targetSdk at least current Google Play requirement.

The first response must be concise and must not write code. It must start:

Hey ADA, this is the work done:
- I reviewed the requirements and I need these decisions before Hello World...

---

## 5. Recommended architecture

### 5.1 Android app

Use native Android with Kotlin and Jetpack Compose.

Recommended libraries:

- Kotlin
- Jetpack Compose Material 3
- Navigation Compose
- Hilt or Koin for dependency injection
- Kotlin Coroutines and Flow
- Room for offline cache
- DataStore for local preferences
- Supabase Kotlin client
- Coil for image loading
- WorkManager for background sync
- Firebase Cloud Messaging only if push/incoming call notifications are required
- Chosen video SDK: Daily, Agora, Twilio, Jitsi, or custom WebRTC

Why native Android:

- Better phone intent handling.
- Better default launcher/home-screen support.
- Better runtime permission UX.
- Better accessibility semantics in Compose.
- More reliable integration with direct call, dialer, WhatsApp, and Android home screen behavior.

### 5.2 Backend

Use Supabase for:

- Postgres database
- Row Level Security
- Supabase Auth
- Supabase Storage private buckets
- Supabase Realtime for contact/gallery/settings updates
- Supabase Edge Functions for token generation, push dispatch, privileged actions, and webhook handling

Never put service role key in the mobile app.

### 5.3 Optional caregiver web dashboard

If ADA wants remote management from a laptop, create a separate web dashboard later:

- Next.js or React
- Supabase Auth
- Supabase Storage upload
- Contact management
- Gallery management
- Device pairing and settings

MVP can use in-app caregiver mode first to reduce scope.

---

## 6. Senior-facing UX rules

### 6.1 Home screen

The senior home screen must have 3 to 6 maximum visible actions.

Recommended MVP home tiles:

1. Call Family
2. Video Call
3. Family Photos
4. WhatsApp Family
5. I Need Help
6. Settings for Family (hidden behind caregiver PIN or long press, not a normal tile)

Each tile:

- Minimum height: 96dp
- Full-width or two-column only if screen size allows very large buttons
- Contact photo or large simple icon
- Label text at least 26sp
- Relation label shown when relevant
- Large visual feedback on tap
- Haptic feedback on tap
- Optional spoken confirmation through Android TextToSpeech

### 6.2 Contact cards

Trusted contact card shows:

- Large face photo
- Name
- Relation
- One primary action
- Optional secondary action only if large and clearly separated

Avoid small overflow menus.

### 6.3 Navigation

- No hamburger menu for senior mode.
- No bottom nav with tiny icons in senior mode.
- Always show a giant "Home" button at the bottom.
- Back button should return to the previous safe screen, never strand the senior in settings.
- Avoid scrolling on the home screen.
- Avoid tabs unless only 2 huge tabs with labels.
- Avoid long press in senior mode.

### 6.4 Error prevention

- For calls, show contact name and photo before action unless Direct Call Mode is enabled by caregiver.
- For WhatsApp, show "Open WhatsApp with [Name]" and "Go back".
- For gallery, no destructive actions in senior mode.
- For emergency, use press-and-hold or two-step confirmation, unless ADA explicitly wants one-tap SOS.

### 6.5 Feedback

Every action must provide feedback:

- Tap visual state
- Haptic vibration if available
- Loading state with plain language
- Error state with "Try again" and "Call instead" fallback
- Confirmation state, such as "Calling Rahul"

### 6.6 Accessibility semantics

Every Compose clickable component must have:

- contentDescription
- role where appropriate
- testTag
- minimum touch target enforcement
- support for Android font scaling up to at least 200 percent
- proper focus order

---

## 7. Feature requirements

### 7.1 Caregiver onboarding

Flow:

1. Caregiver installs app.
2. App asks whether this phone is for "Senior" or "Family Setup".
3. Caregiver authenticates.
4. Caregiver creates or joins a family.
5. Caregiver creates senior profile.
6. Caregiver adds trusted contacts.
7. Caregiver grants permissions: contacts optional, call permission optional, notifications optional, camera/mic for video if chosen.
8. Caregiver chooses Safe Dial Mode or Direct Call Mode.
9. Caregiver chooses WhatsApp shortcuts.
10. Caregiver uploads photos or enables family gallery.
11. Caregiver switches to Senior Mode.
12. Senior sees only the simple home screen.

### 7.2 Senior mode lock

Senior mode should be hard to exit accidentally but not maliciously coercive.

Options:

- Basic: caregiver PIN required to open settings.
- Launcher mode: app can be selected as default home app.
- Android screen pinning: guide caregiver to enable screen pinning if needed.
- Managed device/kiosk: only for owned devices with proper consent and MDM.

Do not prevent emergency access.
Do not hide system safety controls.

### 7.3 Phone calls

User story:

As a senior, I tap a large card with my son's face and the phone starts calling or opens the dialer with his number.

Requirements:

- Contacts are not pulled automatically from the full address book by default.
- Only trusted contacts configured by caregiver appear.
- Store phone numbers in E.164 format.
- Show country code in caregiver mode.
- Validate phone numbers before saving.
- Safe Dial Mode uses ACTION_DIAL.
- Direct Call Mode uses ACTION_CALL only after runtime CALL_PHONE permission.
- Emergency numbers always use ACTION_DIAL.
- After failed call launch, show fallback: "Phone app did not open. Try WhatsApp or ask family."

### 7.4 WhatsApp trusted shortcut

User story:

As a senior, I tap "WhatsApp Rahul" and the app opens Rahul's chat, not the whole contact list.

Requirements:

- Store WhatsApp phone in E.164.
- Build URL as https://wa.me/<digits_only_without_plus>.
- Prefer opening official WhatsApp package when installed.
- Also support WhatsApp Business package if configured.
- If not installed, show a simple explanation and call fallback.
- Never auto-send a message.
- Never open a generic share sheet for senior mode.
- Never ask the senior to pick a contact.

Implementation hint:

- Use ACTION_VIEW with https://wa.me/number.
- Optionally setPackage("com.whatsapp") or setPackage("com.whatsapp.w4b") after checking package availability.
- Use fallback browser intent only after clear confirmation.

### 7.5 Family gallery

User story:

As a senior, I tap "Family Photos" and see recent photos from family without being able to accidentally post, forward, delete, or message random people.

Requirements:

- Use Supabase Storage private bucket.
- Use gallery_items metadata table.
- Family upload from caregiver mode.
- Senior gets read-only gallery.
- Cache thumbnails and recent photos locally for offline viewing.
- Large controls: Next, Back, Home, Play/Pause for videos if enabled.
- Captions can be read aloud.
- No social sharing in senior mode by default.
- Caregiver can delete photos.
- RLS ensures only family members can access family media.

### 7.6 Video calls

User story:

As a senior, I tap "Video Call Rahul" and the app starts a video call with Rahul, or falls back to a normal call if Rahul is unavailable.

Requirements:

- Do not assume WhatsApp provides a reliable public deep link to start a video call.
- Abstract provider behind VideoCallProvider.
- If using Jitsi, create deterministic but unguessable room names server-side.
- If using Daily/Agora/Twilio, generate tokens in Supabase Edge Functions, never in mobile app.
- Request camera and microphone permissions with caregiver setup, not during senior panic moment.
- Use giant "End Call" button.
- Use giant "Call by phone instead" fallback.
- Disable or hide complex meeting controls in senior mode if SDK allows.
- Incoming call UI must be extremely simple: "Rahul is calling" with "Answer" and "Not now".

### 7.7 I Need Help button

Requirements:

- Configurable by caregiver.
- Can call first emergency contact, SMS/WhatsApp family group, or show medical info.
- Avoid accidental activation: default press-and-hold for 2 seconds or confirmation.
- If ADA wants one-tap emergency, clearly mark it red/high contrast and separate it from other buttons.

### 7.8 Caregiver mode

Caregiver mode should include:

- Family members
- Senior profiles
- Trusted contacts
- Shortcut ordering
- Call mode
- WhatsApp phone mapping
- Gallery upload/delete
- Font size and contrast preference
- Launcher/setup guide
- Test buttons: test call, test WhatsApp, test video, test notification
- Audit log for changes

Caregiver mode requires PIN or authenticated user.

---

## 8. Data model

Use this as the initial Supabase schema. Claude must generate migrations, then test them with Supabase local or a Supabase project.

### 8.1 Tables

Core entities:

- profiles
- families
- family_members
- senior_profiles
- senior_devices
- trusted_contacts
- shortcut_tiles
- gallery_items
- call_events
- audit_events
- device_settings

### 8.2 SQL draft

```sql
create extension if not exists pgcrypto;

create type public.app_role as enum ('senior', 'caregiver', 'admin');
create type public.member_role as enum ('owner', 'caregiver', 'viewer');
create type public.shortcut_type as enum ('phone_call', 'video_call', 'whatsapp_chat', 'gallery', 'help', 'custom_app');
create type public.media_type as enum ('image', 'video');
create type public.call_type as enum ('phone', 'video', 'whatsapp');
create type public.call_status as enum ('started', 'completed', 'missed', 'failed', 'cancelled');

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
  accessibility_config jsonb not null default '{"fontScale":"large","contrast":"high","voicePrompts":true}'::jsonb,
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
```

### 8.3 RLS draft

Claude must enable and test RLS. Use helper functions. Do not ship without tests.

```sql
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

-- Profiles: users can read/update themselves.
create policy profiles_select_self on public.profiles
for select to authenticated
using (id = auth.uid());

create policy profiles_update_self on public.profiles
for update to authenticated
using (id = auth.uid())
with check (id = auth.uid());

-- Families: family members can read, creator can insert.
create policy families_select_member on public.families
for select to authenticated
using (public.is_family_member(id));

create policy families_insert_creator on public.families
for insert to authenticated
with check (created_by = auth.uid());

-- Generic family-scoped select policies.
create policy senior_profiles_select_family on public.senior_profiles
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

-- Admin/caregiver writes.
create policy senior_profiles_write_admin on public.senior_profiles
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
```

### 8.4 Storage buckets

Create a private bucket:

- family-gallery

Rules:

- Buckets private by default.
- Storage paths should be family-scoped: family_id/senior_profile_id/item_id.ext.
- Use signed URLs or authorized downloads.
- Do not make family photos public.

Supabase Storage source: https://supabase.com/docs/guides/storage/buckets/fundamentals
Supabase RLS source: https://supabase.com/docs/guides/database/postgres/row-level-security

---

## 9. Android technical implementation details

### 9.1 Project structure

Suggested modules/packages:

```text
app/
  src/main/java/com/ada/onetapfamily/
    MainActivity.kt
    App.kt
    core/
      accessibility/
      permissions/
      telemetry/
      utils/
    data/
      local/          # Room, DataStore
      remote/         # Supabase clients
      repository/
    domain/
      model/
      usecase/
      provider/
        PhoneCallProvider.kt
        WhatsAppProvider.kt
        VideoCallProvider.kt
    features/
      seniorHome/
      contacts/
      calling/
      whatsapp/
      gallery/
      caregiver/
      onboarding/
      settings/
    ui/
      components/
      theme/
      senior/
    workers/
```

### 9.2 Providers

Create abstractions first:

```kotlin
interface PhoneCallProvider {
    fun openDialer(phoneE164: String): Result<Unit>
    fun placeDirectCall(phoneE164: String): Result<Unit>
}

interface WhatsAppProvider {
    fun openTrustedChat(whatsappE164: String): Result<Unit>
    fun isWhatsAppInstalled(): Boolean
}

interface VideoCallProvider {
    suspend fun startOutgoingCall(seniorProfileId: String, contactId: String): Result<Unit>
    suspend fun answerIncomingCall(callId: String): Result<Unit>
    fun endCall(): Result<Unit>
}
```

### 9.3 Phone call logic

- Validate E.164 in caregiver mode.
- Safe dial: ACTION_DIAL + tel: URI.
- Direct call: ACTION_CALL + tel: URI + CALL_PHONE permission.
- No emergency direct calls through ACTION_CALL.
- Record call_events only for launch attempts and statuses visible to our app. Do not claim actual carrier call duration unless integrated with Telecom APIs and permissions.

### 9.4 WhatsApp logic

Pseudocode:

```kotlin
val digitsOnly = e164.removePrefix("+").filter { it.isDigit() }
val uri = Uri.parse("https://wa.me/$digitsOnly")
val intent = Intent(Intent.ACTION_VIEW, uri)

if (isPackageInstalled("com.whatsapp")) {
    intent.setPackage("com.whatsapp")
} else if (isPackageInstalled("com.whatsapp.w4b")) {
    intent.setPackage("com.whatsapp.w4b")
}

startActivity(intent)
```

Do not auto-send messages.
Do not open generic chooser in senior mode.

### 9.5 Launcher mode manifest concept

Add a separate launcher activity only if ADA chooses launcher mode.

```xml
<activity
    android:name=".SeniorLauncherActivity"
    android:exported="true"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

Also keep normal launcher entry point:

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```

Do not force default home app programmatically in consumer mode.

### 9.6 Android versions

- minSdk: 26 unless ADA needs older devices.
- targetSdk: meet current Google Play requirement; as of Google Play policy from Aug 31, 2025, new apps and updates must target Android 15 API 35 or higher. Prefer latest stable compile/target available in local SDK.
- Use Android 13+ notification permission handling.
- Test font scale, large display size, TalkBack, high contrast text, and dark/light modes.

Source: https://developer.android.com/google/play/requirements/target-sdk

---

## 10. Screens and acceptance criteria

### 10.1 SeniorHomeScreen

Acceptance criteria:

- Shows 3 to 6 giant action tiles.
- No tiny controls.
- Tapping Call Family opens trusted contacts or default favorite.
- Tapping Family Photos opens read-only gallery.
- Tapping Home while already home does nothing but gives gentle feedback.
- Works with font scale 200 percent.
- Works in portrait and large-screen layout.
- No crash offline.

### 10.2 TrustedContactsScreen

Acceptance criteria:

- Shows only active trusted contacts.
- Each contact has face photo placeholder if no photo.
- Each action is explicit: "Call", "Video", "WhatsApp".
- No contact picker for senior.
- Sorting is caregiver-controlled.

### 10.3 CallConfirmScreen

Acceptance criteria:

- Shows "Call [Name]?" with photo.
- Primary button is very large: "Call now".
- Secondary button is very large: "Go back".
- If Direct Call Mode is enabled, caregiver can bypass this for selected contacts only.

### 10.4 GalleryScreen

Acceptance criteria:

- Opens latest family photos in one tap from home.
- Big thumbnails and full-screen viewer.
- No share, upload, forward, post, story/status, or delete in senior mode.
- Gallery works offline for cached recent items.
- Captions readable and optionally spoken.

### 10.5 WhatsAppConfirmScreen

Acceptance criteria:

- Shows "Open WhatsApp with [Name]?".
- Clear explanation: "You will leave the simple screen. Press Home here to come back."
- Opens only the trusted contact link.
- If WhatsApp missing, offer normal call fallback.

### 10.6 CaregiverMode

Acceptance criteria:

- Protected by auth or PIN.
- Can add/edit/delete trusted contacts.
- Can upload/delete photos.
- Can configure senior home tiles.
- Can test every shortcut.
- Cannot save invalid phone numbers.
- Writes audit_events.

---

## 11. Privacy, security, and safety

Non-negotiable:

- No ads.
- No selling data.
- No unnecessary tracking.
- No service role key in mobile app.
- RLS enabled on every public table.
- Private bucket for family photos.
- No WhatsApp scraping.
- No hidden message sending.
- No automatic posting to Stories/Status.
- No dark patterns to trap a senior in the app.
- Caregiver access must be explicit and auditable.
- Provide a simple privacy note in caregiver mode.

Sensitive data:

- Family photos
- Names and relationships
- Phone numbers
- Call attempt logs
- Device settings

Minimize call logs. Do not store actual call audio or video recordings.

---

## 12. Testing plan

### 12.1 Automated tests

- Unit tests for phone number formatting.
- Unit tests for WhatsApp URL building.
- Unit tests for permission state transitions.
- Repository tests with fake Supabase client.
- UI tests for senior home tiles at fontScale 1.0, 1.5, 2.0.
- UI tests for no small touch targets where possible.
- Screenshot tests for senior home.
- RLS tests for family isolation.

### 12.2 Manual accessibility tests

Run on at least:

- Small Android phone
- Large Android phone
- Low-end Android phone if possible
- Font size: largest
- Display size: largest
- TalkBack on
- High contrast text on
- Reduced animations if available
- One-handed use
- Shaky-hand simulation: verify spacing and large targets

### 12.3 User testing script

Test with older adults or proxies where possible:

1. "Call your daughter."
2. "Look at yesterday's family photo."
3. "Open WhatsApp with your son."
4. "Go back home."
5. "Ask for help."

Measure:

- Number of taps
- Hesitation points
- Wrong taps
- Whether user can recover without help
- Emotional reaction: confidence, fear, frustration

---

## 13. Build milestones

### Milestone 0 - Decisions before Hello World

Deliver:

- ADA decision checklist answered.
- Final stack selected.
- Video provider chosen or stubbed.
- Supabase project details available.

### Milestone 1 - Foundation

Deliver:

- Native Android project.
- Compose theme with senior accessibility tokens.
- Supabase client configured through local config.
- No secrets committed.
- Basic navigation.
- README.

### Milestone 2 - Senior home and contacts

Deliver:

- SeniorHomeScreen.
- Trusted contacts local fake data.
- Big senior cards.
- Safe dial mode.
- WhatsApp URL builder and missing-app fallback.

### Milestone 3 - Supabase integration

Deliver:

- Migrations.
- RLS policies.
- Auth.
- Contact sync.
- Offline cache.

### Milestone 4 - Gallery

Deliver:

- Private storage bucket integration.
- Upload from caregiver mode.
- Read-only senior gallery.
- Offline thumbnails.

### Milestone 5 - Video calling

Deliver:

- VideoCallProvider implementation for chosen provider.
- Camera/mic permission flow.
- Senior outgoing call UI.
- Incoming call handling if provider/notifications selected.

### Milestone 6 - Launcher mode and hardening

Deliver:

- Optional launcher activity.
- Caregiver setup guide.
- Accessibility audit.
- Privacy audit.
- Production build docs.

---

## 14. Required output from Claude Code

When coding starts, Claude must deliver:

1. Project files.
2. README.md with setup instructions.
3. .env.example or local.properties.example.
4. Supabase SQL migrations.
5. RLS policy tests or documented manual test queries.
6. Android implementation.
7. Unit/UI tests.
8. A final implementation summary that starts with "Hey ADA,".

Every completed step must say:

Hey ADA, this is the work done:
- Done: ...
- Files changed: ...
- How to run: ...
- What still needs your decision: ...

---

## 15. Things Claude must not do

- Do not build a generic chat app.
- Do not add unnecessary menus, tabs, feeds, stories, posts, reactions, or likes.
- Do not use tiny material defaults without senior overrides.
- Do not ask the senior to configure settings.
- Do not rely on WhatsApp for read-only photo viewing.
- Do not claim direct WhatsApp video call works unless verified with official/current docs or device testing.
- Do not store service role key in Android code.
- Do not make photos public.
- Do not force launcher/kiosk behavior without ADA consent.
- Do not use AccessibilityService to automate other apps without explicit approval and policy review.
- Do not skip RLS.

---

## 16. Copy-paste startup response expected from Claude

The first response from Claude should be similar to:

Hey ADA, this is the work done:
- Done: I reviewed the senior smartphone assistant requirements and identified the decisions needed before Hello World.
- Need from you: Please confirm platform, app name, video provider, calling mode, WhatsApp scope, gallery upload method, auth method, Supabase URL/anon key, and distribution method.
- Risks or tradeoffs: Supabase does not provide video media streaming by itself, and WhatsApp cannot be made read-only by a normal app. I recommend an in-app family gallery and a chosen video provider.
- Next step I will take after your answer: I will create the Android Kotlin project, Supabase migrations, senior-first Compose UI foundation, and the first working call/WhatsApp/gallery MVP.

Then list the questions from Section 4.

---

## 17. ADA decisions — confirmed 2026-06-12

- Platform: Android-first native Kotlin + Compose. iOS wanted later (revisit KMP vs separate Swift app when iOS starts).
- App name: **Mudra** (user-visible). Package: `com.ada.mudra`.
- Distribution: sideload/dev build first.
- Video provider: deferred (VideoCallProvider stub exists; Jitsi default for prototype when chosen).
- Calling mode: Safe Dial (ACTION_DIAL).
- WhatsApp: trusted chat shortcut only via wa.me.
- Gallery upload: in-app caregiver (Family Setup) mode; local-only in MVP.
- Languages: English, Malayalam, Hindi (per-app locale via AppCompatDelegate).
- Haptics: ON and strong (80ms full-amplitude pulse on every senior tap); toggle in Settings.
- Home screen: optional launcher mode via disabled-by-default activity-alias `com.ada.mudra.SeniorLauncherAlias`, toggled in Settings.
- Caregiver control: ADA wants the son/daughter to control the senior's app interface remotely. MVP ships on-device Family Setup behind a PIN (default 1234); remote control = next milestone, requires Supabase URL + anon key from ADA.
- Supabase credentials: NOT yet provided. MVP is local-only (DataStore + app-private files).
- minSdk 26, targetSdk 35, Kotlin 2.0.21, AGP 8.7.3, Gradle 8.10.2, Compose BOM 2024.12.01.
