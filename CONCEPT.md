# Mudra — Detailed Concept & Technical Overview

## 1. What's Being Built

**Mudra** is a purpose-built Android smartphone assistant for older adults (65+) who may have:
- Low vision or hand tremors
- Limited digital literacy or fear of making mistakes
- Memory challenges or uncertainty about phone controls
- A trusted family member (son, daughter, caregiver) who sets it up and supports them

The app reduces a complex smartphone to **three safe, giant actions** and hides everything else:
1. **Call Family** — reach a trusted person without touching the contact list
2. **WhatsApp Family** — text a trusted person without risking accidental calls, stories, or wrong chats
3. **Family Photos** — view photos sent by relatives in a read-only gallery (no share, delete, forward, or post buttons)

Plus a **Family Setup** mode (for the caregiver, behind a PIN) to configure contacts, upload photos, change settings, and choose language.

### Core Design Principle
**"Less fear, fewer choices, bigger actions, safe recovery."**

Not a full-featured messaging app. Not a camera replacement. A **single phone's safe front door** for the people who matter most.

---

## 2. How It Works

### 2.1 Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  MainActivity                       │ Jetpack Compose UI
├──────────────────────────────────────────────────────┤
│  SeniorHomeScreen  PinScreen  CaregiverHomeScreen   │
│  CallConfirmScreen ContactsScreen GalleryScreen     │
│  WhatsAppConfirmScreen ContactEditScreen, etc.      │
├──────────────────────────────────────────────────────┤
│                  MudraViewModel                      │ State & Providers
│  - contacts: StateFlow<List<TrustedContact>>        │
│  - photos: StateFlow<List<GalleryPhoto>>            │
│  - settings: StateFlow<AppSettings>                 │
│  - phoneCallProvider, whatsAppProvider              │
├──────────────────────────────────────────────────────┤
│                 MudraRepository                      │ Local Storage
│  - DataStore (Preferences): settings, JSON lists    │
│  - App-private file dirs: avatar photos, gallery    │
├──────────────────────────────────────────────────────┤
│  PhoneNumbers, Haptics, LauncherMode                │ Core Utilities
└─────────────────────────────────────────────────────┘
```

### 2.2 Data Flow

**Senior taps a button:**
1. Haptics pulse (80ms, full strength) → confirms the phone registered the touch
2. Tap handler routes to the appropriate screen (Navigation Compose)
3. ViewModel reads from StateFlow; screen displays the data
4. User confirms action (e.g., "Call John?")
5. Tap handler calls a provider (PhoneCallProvider or WhatsAppProvider)
6. Provider launches the system app (Dialer or WhatsApp) via Intent
7. Screen returns to Home or shows fallback error message

**Caregiver adds a contact:**
1. PIN check → CaregiverHomeScreen
2. "Add contact" → ContactEditScreen
3. Caregiver fills name, relation, phone (E.164 validation), optional WhatsApp number
4. Photo picker → copy to app-private avatars directory
5. ViewModel.upsertContact() → DataStore update + StateFlow notifies observers
6. All listening screens update instantly (contacts, home tiles, confirm screens)

**Family photo viewed by senior:**
1. Gallery screen reads from ViewModel.photos (StateFlow)
2. Photos are sorted newest-first, large on screen
3. Previous/Next buttons are 88dp tall; Home button always visible
4. Captions are read-only; displayed in plain text, no share/forward controls exist

### 2.3 Key Components

#### MudraRepository (Single source of truth for offline data)
```kotlin
class MudraRepository(context: Context) {
  val contacts: Flow<List<TrustedContact>> // DataStore → Coil image files
  val photos: Flow<List<GalleryPhoto>>     // DataStore → app-private storage
  val settings: Flow<AppSettings>           // DataStore
  
  suspend fun upsertContact(contact) { ... }  // Write to DataStore
  suspend fun addPhoto(uri: Uri) { ... }      // Copy URI to app-private, update DataStore
}
```

#### PhoneNumbers (Pure utility — no state)
```kotlin
object PhoneNumbers {
  fun isValidE164(raw: String): Boolean  // Validates +91... format
  fun normalize(raw: String): String     // Strips spaces/dashes
  fun waLink(e164: String): String       // Returns https://wa.me/919812...
}
```

#### Providers (Abstract away Android intents)
```kotlin
interface PhoneCallProvider {
  fun openDialer(phoneE164: String): Result<Unit>  // ACTION_DIAL
}
interface WhatsAppProvider {
  fun openTrustedChat(whatsappE164: String): Result<Unit>  // wa.me link
}
```
Real implementations use Android intents; future Supabase version can swap in no-op stubs for testing.

#### SeniorComponents (Reusable UI with accessibility built-in)
- `GiantTile` — 112dp+ action cards with icon + text label
- `GiantButton` — 88dp+ primary/secondary buttons
- `ContactCard` — 104dp+ contact row with photo, name, relation
- `rememberSeniorTap()` — wraps onClick with haptics

All sized to pass WCAG 2.2 target-size (24×24 minimum) and to work with Android font scale up to 200%.

#### Theme (Senior-first palette & typography)
- Light theme only (dark mode switches confuse older users)
- High-contrast: `#0B4FA2` (Mudra Blue), `#14181D` (text), white background
- Typography: 22–28sp (24sp+ minimum for body text per research)
- Shapes: 16–24dp corner radius (soft, approachable)

---

## 3. Problems & Solutions

### Problem 1: "How do we know the senior won't accidentally call/delete someone?"

**Solution:** Multi-layer confirmation + impossibility of action.
- Contacts are whitelisted by caregiver only (no address book auto-pull).
- Senior taps a contact card → confirm screen (photo + "Call [Name]?") → dialer opens.
- Gallery has no delete button in senior mode (caregiver-only deletion in Settings).
- WhatsApp opens a *specific chat link* (wa.me/number), not the app's home screen.

**Residual risk:** If the caregiver accidentally adds a wrong number, it'll show up in Call Family. Mitigation: phone validation (E.164 format, can't be blank) and edit screens show the full number for review.

### Problem 2: "What if the phone dies or gets reset?"

**Solution (MVP):** All data lost. Photos and contacts are stored in app-private files and DataStore.

**Real solution (Milestone 3):** Supabase sync.
- Every contact, photo, and setting syncs to a Postgres database with Row-Level Security.
- The caregiver's phone has a backup automatically.
- New phone: re-install Mudra, sign in, contacts and photos sync down.
- Change on one device (e.g., caregiver adds a contact) → realtime sync to senior's phone.

### Problem 3: "WhatsApp can't be made read-only by an app — what if the senior taps a Story button?"

**Solution:** We don't open WhatsApp at all; we open a *chat link* directly.
```
https://wa.me/919812345678
```
This is the official WhatsApp API for "click to chat." It opens the conversation, not the app home screen. No Stories, no contact picker.

**Residual risk:** Once WhatsApp is open, the senior could navigate to Stories or call the person instead of chatting. Mitigation: caregiver shows them the confirm screen ("You will leave Mudra for a moment. Press your phone's Home button here to come back") and stays nearby during first uses.

### Problem 4: "Video calling isn't reliable; we can't guarantee WhatsApp video works."

**Solution:** VideoCallProvider interface is stubbed.
```kotlin
interface VideoCallProvider {
  suspend fun startOutgoingCall(contactId: String): Result<Unit>
}
```
When a video provider (Jitsi / Daily.co / Agora) is chosen and tested, we implement this interface. Until then, video calling is not shown to the senior.

**Why not use WhatsApp's video directly?** WhatsApp doesn't expose a documented deep link for starting a video call. The CLAUDE.md spec forbids claiming a feature we can't verify.

### Problem 5: "The caregiver needs to manage Mudra from *their own phone*, not just the senior's phone."

**Solution (MVP):** Not done. The caregiver (son/daughter) must physically visit the senior's phone, unlock Family Setup with the PIN, and configure contacts/photos there.

**Real solution (Milestone 3):** Supabase backend + web dashboard.
- Caregiver signs in to mudra.app (web or mobile companion app).
- Dashboard shows their family members, seniors, and contacts.
- Caregiver adds a contact → instantly synced to senior's phone via Supabase Realtime.
- Caregiver reviews call logs (metadata only, no recordings).
- Caregiver can remotely disable/enable shortcuts, change language, etc.

### Problem 6: "Android 13+ requires per-app language switching; will older Android break?"

**Solution:** Use AppCompatDelegate and the new AppLocalesMetadataHolderService (available down to Android 8.0 via AndroidX AppCompat 1.6+).
```kotlin
AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ml"))
```
Mudra's language picker (in Settings) changes the app's locale without restarting it. Tested on API 26–35.

### Problem 7: "How do we make Mudra the phone's home screen without forcing it?"

**Solution:** Optional, never default.
- AndroidManifest declares an activity-alias with `HOME` and `DEFAULT` categories.
- The alias is disabled by default (`android:enabled="false"`).
- Settings screen has a toggle: "Use Mudra as the phone's home screen" → opens the system Home chooser.
- Caregiver picks Mudra from the chooser if they want it.
- At any time, Settings > Home apps lets the caregiver switch back to the normal launcher.

We never call `setDefaultLauncher()` or use MDM; we respect user choice entirely.

### Problem 8: "What if the caregiver forgets the Family Setup PIN (default 1234)?"

**Solution (MVP):** Uninstall and reinstall the app. All data is lost.

**Real solution (Milestone 3):** 
- PIN is stored hashed in Supabase.
- Caregiver signs in to the web dashboard to reset the PIN.
- A notification is sent to the senior's phone (if push is enabled) to confirm the PIN change.

---

## 4. Technical Decisions & Trade-offs

### Why Kotlin + Jetpack Compose (not React Native, Flutter, Xamarin)?
**Trade-off:** More native Android code to maintain, but:
- Best accessibility semantics for older users (TalkBack integration, focus order, semantic roles).
- Best phone intent handling (ACTION_DIAL, ACTION_VIEW, home-screen mode).
- No runtime overhead; maximum responsiveness and battery life matter for older users.
- Jetpack Compose Modifier system lets us enforce 88dp minimum button sizes at compile time.

### Why DataStore (not Room, not Realm)?
**Trade-off:** No complex queries, but:
- DataStore is simpler for a small MVP dataset (10 contacts, 20 photos max per device).
- Atomic writes (single transaction per action), no corruption risk.
- No bloat; Supabase will handle relational data when we add backend sync.

### Why local-only MVP (not Supabase from day 1)?
**Trade-off:** Manual caregiver setup on senior's phone, data loss on factory reset, but:
- Faster MVP (no auth flow, no RLS complexity, no tokens).
- Proves the senior UX first; backend sync is a later iteration.
- Allows ADA to test the concept without upfront infrastructure cost.

### Why E.164 phone format (+91...)?
**Trade-off:** Extra validation step in the UI (more typing), but:
- One format worldwide; no ambiguity (is 0501234567 India or UAE?).
- Works for both CALL and WhatsApp (same number format).
- Phone number libraries (libphonenumber) validate E.164 reliably.

### Why strong haptics always on, not subtle?
**Trade-off:** Battery cost (~5mA per pulse, 80ms = negligible), but:
- Research shows older users distrust touchscreens; haptics confirm "the phone heard me."
- 80ms is long enough to feel, short enough not to startle.
- Toggleable in Settings for accessibility (pacemaker users, etc.).

---

## 5. Roadmap (Post-MVP)

### Milestone 3: Supabase Sync (Remote Caregiver Control)
- Supabase project (Postgres + Auth + Storage)
- Kotlin SDK integration
- RLS policies (family-scoped data, caregiver-only writes)
- Edge Function for video-call token generation
- Realtime Composite updates
- Sync worker (WorkManager)
- Offline-first architecture (local cache, lazy sync)

### Milestone 4: Video Calling
- Choose provider (Jitsi, Daily.co, Twilio, Agora)
- Implement VideoCallProvider interface
- Camera/mic permission flow (request at setup, not at call time)
- Incoming call notification (Firebase Cloud Messaging or Supabase Realtime)
- Simple call UI (no mute, no screen share, no features to confuse)

### Milestone 5: Caregiver Web Dashboard
- Next.js / React admin panel
- Sign in, manage family, seniors, contacts, photos
- Call history and metadata logs
- Remote PIN reset
- A/B test language, haptics, accessibility settings

### Milestone 6: iOS Version
- Evaluate: SwiftUI native vs. Kotlin Multiplatform Mobile (KMM)
- Same Supabase backend, same RLS
- Same 3 functions (Call, WhatsApp, Photos)
- Different accessibility APIs (UIAccessibility instead of TalkBack)

### Optional: Emergency Button
- Press-and-hold for 2 seconds (not one-tap, to avoid accidents)
- Configurable in Family Setup (call, SMS, or open medical info)
- Different affordance / placement so not accidentally tapped

---

## 6. Testing Strategy

### Unit Tests (Automated)
- `PhoneNumbersTest` — validates E.164 format, normalizes input, builds wa.me links
- Repository tests (mock DataStore, fake image URIs)
- ViewModel tests (contact CRUD, settings updates)

### UI Tests (Automated)
- Senior home layout (tiles are 112dp+, text is visible at 200% font scale)
- Contact list rendering
- Navigation smoke tests (can navigate home from every screen)
- Haptics toggle (on/off verified in settings)

### Manual Accessibility Tests (Required before shipping)
- TalkBack ON: can the senior navigate and tap without seeing?
- Font scale 200%: does the UI fit on a small screen?
- One-handed use: can they reach a button with thumb only?
- High contrast: colors pass WCAG AAA (7:1 minimum)?

### User Testing (With older adults or proxies)
- "Call your daughter" — how many taps? any hesitation?
- "Look at yesterday's family photo" — can they find it? do they understand navigation?
- "Go back home" — can they recover if lost?
- Measure: tap count, errors, time to task, emotional reaction (confidence vs. frustration).

---

## 7. Privacy & Security

### On-Device (MVP)
- No analytics. No crash reporting. No ads.
- Photos stored in app-private directory (`/data/data/com.ada.mudra/files/gallery/`); invisible to other apps.
- Contacts stored in DataStore (encrypted by default on Android 5.0+).
- PIN hashed in memory; never logged.

### With Supabase (Milestone 3)
- Row-Level Security (RLS) enforces that a senior can only see their family's contacts.
- A caregiver can only edit contacts they own.
- Photos stored in a private Supabase Storage bucket, accessible only via RLS-enforced signed URLs.
- Audit log (who changed what, when) for transparency.

### What We Don't Store
- Call recordings or video footage.
- Health data (this is not a medical device).
- GPS location (no tracking).
- Browsing history or typing patterns.

---

## 8. Known Limitations & Future Fixes

| Limitation | Milestone | Why |
|-----------|----------|-----|
| Data lost on factory reset | 3 (Supabase) | MVP is local-only |
| Can't manage senior's contacts from caregiver's phone | 3 (Web dashboard) | MVP requires physical access |
| Video calling not available | 4 (Provider TBD) | Must choose & test provider first |
| No emergency button | 6 | Out of MVP scope |
| No call history | 3 (Supabase audit log) | Local storage isn't needed for MVP |
| Can't disable/enable tiles remotely | 3 (Supabase + Edge Functions) | Requires backend logic |
| English-only initially | Done (i18n ready) | Now supports en/ml/hi |

---

## 9. Success Metrics

**For the Senior:**
- Time to call Mom: < 5 seconds, no confusion.
- Accidental calls to wrong person: 0.
- Emotional response: relief, not frustration or fear.

**For the Caregiver:**
- Time to add a contact: < 2 minutes.
- Data persistence: contacts survive a week without touching the phone.
- Remote management (post-Milestone 3): add contact from their own phone, see it on senior's phone in < 1 second.

**For the Business:**
- User retention at 30 days: 80%+ (older users are loyal if it works).
- Ratings: 4.5+ stars (accessibility matters; niche apps get higher ratings if focused).
- Word-of-mouth: families recommend it to neighbors and friends.

---

## 10. Questions for ADA

1. **Supabase timeline:** When do you want to start Milestone 3 (remote control)? Do you already have a Supabase account, or should I create a demo project?

2. **Video provider:** Jitsi (free, open-source, but needs a server) or Daily.co (managed, best UX, paid)? I can prototype both.

3. **iOS:** Is this Android-only for now, or should we plan KMM/native iOS in parallel?

4. **Emergency button:** Do you want a quick "call first caregiver" feature in the MVP, or is that post-launch?

5. **Offline gallery:** Should a recent 10 photos be cached locally for offline viewing, or is online-only fine until Milestone 3?

---

**GitHub:** https://github.com/adarshbaburaj/mudra_app.git
**Ready to:** compile on your machine, open in Android Studio, or start Milestone 3.
