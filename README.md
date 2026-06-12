# Mudra — a calm phone for our parents

Mudra is an Android app that gives older adults a simple, fearless way to use
a smartphone: three giant buttons, trusted people only, and no way to
accidentally delete, forward, post or call a stranger.

Built per the product spec in [claude.md](claude.md).

## What the MVP does (3 functions)

1. **Call Family** — giant cards with the photo, name and relation of trusted
   contacts only. Tapping opens a confirm screen, then the phone dialer
   (Safe Dial Mode, `ACTION_DIAL` — no call permission needed).
2. **WhatsApp Family** — opens the WhatsApp chat of one trusted contact via the
   official `https://wa.me/<number>` link. Never the contact list, never
   stories, never auto-send. Falls back to a normal call if WhatsApp is missing.
3. **Family Photos** — a read-only gallery. Big photo, big Previous/Next, big
   Home. No share / delete / upload / story buttons exist on the senior screen.

Plus **Family Setup** (the son/daughter side, behind a PIN):
- Add/edit/remove trusted contacts with photos, relation labels, and per-contact
  Call/WhatsApp visibility. Phone numbers validated as E.164 (`+91…`).
- Add photos with captions to the family gallery; edit and delete them.
- Settings: strong haptics toggle, language (English / മലയാളം / हिन्दी),
  change PIN, and optional "use Mudra as the phone's home screen" mode.

**Default Family Setup PIN: `1234` — change it in Settings on first use.**

## Senior-first design rules baked in

- Home tiles ≥ 112dp tall; primary buttons ≥ 88dp; every control labeled with text.
- Body text 22–24sp, buttons 28sp+, high-contrast light theme, no dark-mode surprises.
- Strong haptic pulse on every senior-facing tap (can be turned off in Settings).
- A giant "Go back Home" escape on every senior screen; no hamburgers, no tiny nav.
- Works with Android font scale up to 200%.

## How to run it

1. Install [Android Studio](https://developer.android.com/studio) (free; it
   bundles the JDK and Android SDK).
2. Open this folder (`File > Open…`). Let Gradle sync finish.
3. Plug in an Android phone with USB debugging on (or create an emulator),
   then press **Run**. Min Android version: 8.0 (API 26).

To try the senior flow: open Family Setup (PIN 1234) → add a contact with a
real number → go back to the simple screen.

Run unit tests with: `./gradlew test`

## Project structure

```
app/src/main/java/com/ada/mudra/
  MainActivity.kt / MudraApp.kt / MudraViewModel.kt
  core/            # phone number + wa.me helpers, haptics, launcher mode
  data/            # models + local DataStore repository (photos in app-private files)
  domain/provider/ # PhoneCallProvider, WhatsAppProvider, VideoCallProvider (stub)
  features/        # seniorhome, contacts, calling, whatsapp, gallery, caregiver
  ui/              # senior theme tokens, giant components, navigation
```

## Privacy

Everything stays on the phone in this MVP: contacts, photos and settings are
stored in app-private storage. Nothing is uploaded, tracked, or shared.

## Family sync (v0.2)

Family Setup now has **Account & Sync**: create an account, create your
family, and contacts + photos sync through Supabase (Postgres with Row Level
Security + a private storage bucket). Sign in with the **same account** on the
parent's phone and on yours — changes made on one phone appear on the other
within a second via Realtime. The senior screens always read the local cache,
so everything keeps working offline.

One-time backend setup: see [supabase/README.md](supabase/README.md)
(two SQL files to paste into the Supabase SQL Editor).

## Release builds

`./gradlew assembleRelease` produces a signed APK at
`app/build/outputs/apk/release/app-release.apk` you can install on any phone
("sideload"). Signing uses `release.keystore` + passwords from the gitignored
`local.properties`. **Back both up** — losing them means installed phones
can't receive updates. Code shrinking (R8) is intentionally off until we've
soak-tested it on devices.

## What's next (not yet built)

- **Video calling** — behind the existing `VideoCallProvider` interface
  (Jitsi for prototype or Daily for product-grade tokens via Edge Functions).
- **Caregiver web dashboard** (Next.js on the same Supabase tables),
  family invites, remote PIN reset.
- **I Need Help button**, caregiver audit log, iOS version (Android-first per spec).
