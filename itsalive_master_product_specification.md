# Feature Specification: ItsAlive — Shared Pet Partnership Game

**Feature Branch**: `001-itsalive-core-product`  
**Created**: 2026-11-12  
**Status**: Ready for Planning  
**Input**: Complete end-to-end product specification covering onboarding, pairing, pet care mechanics, daily photo challenges, physical activity sync, shared locations, home screen widgets, statistics, settings, and edge cases.

---

## 1. Overview & User Value

**ItsAlive** is a mobile lifestyle companion for couples and close friends that turns caring for a shared virtual pet into a collaborative daily habit. Unlike traditional single-player virtual pets, the companion's growth, mood, and survival depend on mutual participation: completing time-limited daily photo moments together, maintaining healthy physical activity (steps and sleep), visiting shared real-world locations, and checking in on one another.

### Scope Boundaries
- **In Scope:** Account creation and profile management, 1-on-1 pairing via invite codes/links, pet egg selection and naming, real-time shared pet room (feeding, playing, streak protection, leveling), daily 60-minute photo challenges with blind-reveal mechanics, partner nudges, optional health activity syncing (steps and sleep), optional shared location check-ins (up to 3 places), home screen glanceable widgets, memory gallery, streak calendar, and full privacy/account controls.
- **Out of Scope:** Group rooms (more than 2 people), public social feeds or leaderboards, real-money in-app purchases/monetization, third-party wearable hardware setup flows, and video/audio messaging.

### Dependencies & Assumptions
- **Two-Player Core Assumption:** The primary game loop assumes two active participants in a shared room; however, single-user actions (taking one's own photo, syncing personal steps, feeding) must remain functional and rewarding while waiting for a partner.
- **System Permissions Assumption:** Users may deny optional system permissions (activity tracking, precise background location, notifications). Core app functionality (photo challenges, manual pet care, streak tracking) must operate without degradation when optional permissions are declined.
- **Time Synchronization Assumption:** All daily challenge countdowns and streak windows are anchored to a universal standard clock so partners in different time zones experience the exact same 60-minute challenge window.

---

## 2. User Scenarios & Testing *(mandatory)*

### User Story 1 — Account Setup & 1-on-1 Pairing (Priority: P1)

A new user signs in, sets up their profile, creates a private room by choosing a pet egg, and invites their partner using a 6-digit code or shareable link. Once the partner joins, the egg hatches into their shared companion.

**Why this priority:** Without authentication and 1-on-1 pairing, no shared gameplay or synchronization can occur.

**Independent Test:** Can be fully tested using two user accounts: User A creates a room and receives a 6-digit code; User B enters the code; both accounts transition simultaneously to the pet naming/hatching step and land in the shared home experience.

**Acceptance Scenarios:**
1. **Given** an unauthenticated user launches the app, **When** they complete sign-in and enter a valid display name (2–24 characters), **Then** their profile is created and they are routed to the pairing setup screen.
2. **Given** User A is on the pairing screen (`Step 01/02`), **When** they select one of four egg colors (Green, Blue, Orange, Purple), **Then** a unique 6-digit room code (e.g., `482-910`) is displayed along with options to copy the code or share an invite link.
3. **Given** User B selects "Have a code? Join a room" and enters User A's valid 6-digit code, **When** they confirm joining, **Then** both users automatically advance to `Step 02/02`, witness the egg hatching animation, confirm the pet's name (defaulting to "Buddy"), and enter the main home screen.
4. **Given** User B taps an invite link directly, **When** the app opens, **Then** the 6-digit code is pre-filled automatically without manual typing.

---

### User Story 2 — Daily 60-Minute Photo Challenge & Blind Reveal (Priority: P1)

Once per day, both partners receive a prompt for a shared photo moment with a 60-minute countdown timer. Each partner's photo remains blurred to the other until both have submitted their photos, rewarding them with streak progression and pet happiness.

**Why this priority:** This is the primary daily retention loop and emotional anchor of the product.

**Independent Test:** Can be tested by triggering a daily challenge in an active room, having User A capture a photo (verifying User B sees a blurred preview), and then having User B capture a photo (verifying both photos unblur and the streak counter increments by 1).

**Acceptance Scenarios:**
1. **Given** an active daily challenge is underway, **When** either partner opens the `Today` screen, **Then** they see the active countdown timer (e.g., `00:42:15 remaining`), the daily prompt text, and two photo slots ("Your Photo" and "Partner's Photo").
2. **Given** neither partner has uploaded a photo, **When** User A taps "Take Photo", captures a live photo via the in-app camera, and confirms it, **Then** User A's photo displays with an "Uploaded" badge, User B's slot remains blurred with a "Waiting..." status, and User A's primary action button changes to "Nudge Partner".
3. **Given** User A has uploaded a photo and User B has not, **When** User B opens the app, **Then** User B sees User A's photo blurred with a "Ready to reveal!" status and a primary button labeled "Take Photo to Reveal".
4. **Given** both partners upload their photos before the 60-minute timer expires, **When** the second photo completes uploading, **Then** both photos immediately unblur for both users, the shared streak increments by `+1 Day`, and the pet gains `+30 Happiness` and `+50 XP`.
5. **Given** User A has uploaded a photo and is waiting for User B, **When** User A taps "Nudge Partner", **Then** User B receives a friendly push notification reminder, and the nudge button enters a 15-minute cooldown to prevent spam.

---

### User Story 3 — Shared Pet Room, Care Actions & Streak Protection (Priority: P1)

Partners visit their pet's room to monitor its growth stage, level, XP progress, energy, and mood, and interact with it through feeding, playing, and streak-saving actions.

**Why this priority:** Provides immediate interactive feedback and tangible progression tied to the couple's daily habits.

**Independent Test:** Can be tested by opening the `Pet Room` tab, performing `Feed` and `Play` actions, verifying stat increases, XP bar progression, cooldown enforcement, and testing `Save Streak` when a streak is marked at risk.

**Acceptance Scenarios:**
1. **Given** a user opens the `Pet Room` tab, **When** the screen loads, **Then** they see the pet's growth stage (`BABY` for Lvl 1–2, `TEEN` for Lvl 3–6, `ADULT` for Lvl 7+), current level, XP progress bar (`currentXP / level * 100`), animated pet avatar, mood badge (`HAPPY`, `HUNGRY`, `SLEEPY`, `LONELY`, or `SICK`), and progress bars for `Energy (0–100%)` and `Happiness (0–100%)`.
2. **Given** the couple has at least 1 food item in inventory and the feeding cooldown (4 hours) is inactive, **When** a user taps `Feed` and selects a treat, **Then** the pet plays a happy eating animation, `Energy` increases by `+10%` (capped at `100%`), `XP` increases by `+15`, and food inventory decreases by `1`.
3. **Given** the play cooldown (4 hours) is inactive, **When** a user taps `Play` and completes the brief interactive play gesture, **Then** `Happiness` increases by `+10%` (capped at `100%`) and `XP` increases by `+15`.
4. **Given** a daily challenge expired without both partners completing it, **When** the streak enters the 12-hour `AT_RISK` grace window, **Then** the `Save Streak` shield button becomes active; tapping it consumes `100 XP` (or the weekly free shield) to restore the streak instead of resetting it to `0`.

---

### User Story 4 — Passive Health Activity Sync (Steps & Sleep) (Priority: P2)

When enabled by the user, daily step counts and sleep duration automatically sync from the device's health repository to replenish the pet's energy and grant food treats.

**Why this priority:** Connects real-world healthy habits to the virtual pet without requiring manual logging, deepening long-term engagement.

**Independent Test:** Can be tested by granting health read permissions, recording or simulating 8,000+ steps and 7+ hours of sleep, and verifying the pet's energy increases and daily goal rewards are granted once per day.

**Acceptance Scenarios:**
1. **Given** a user grants permission to read step count and sleep duration, **When** their daily step count reaches `8,000 steps` (contributing toward the couple's `16,000` combined daily target), **Then** the pet automatically gains `+25 Energy`, `+20 XP`, and `+1 food treat` (awarded once per calendar day per user).
2. **Given** a user's recorded sleep reaches `7 hours (420 minutes)` or more, **When** activity data syncs, **Then** the pet gains `+15 Energy` and `+10 XP` (awarded once per calendar day per user).
3. **Given** a user declines health permissions, **When** they use the app, **Then** no blocking prompts appear, the health card in the `Pair` tab remains marked as optional, and the user can still maintain 100% pet energy via daily challenges and feeding.

---

### User Story 5 — Shared Places & Co-Presence Bonuses (Priority: P3)

Partners can save up to 3 meaningful real-world locations (e.g., home, favorite cafe, park). When both partners are physically present within 100 meters of a saved place at the same time, the pet receives a joint outing bonus.

**Why this priority:** Enhances real-world offline connection beyond digital check-ins.

**Independent Test:** Can be tested by adding a saved place on the map picker and simulating both partners entering the 100-meter radius for at least 15 minutes, verifying the bonus notification and stat boost trigger once per day.

**Acceptance Scenarios:**
1. **Given** a pair has fewer than 3 saved places, **When** either partner opens `Shared Places` in the `Pair` tab, selects a location on the map, and gives it a title, **Then** the place is saved for both partners with a 100-meter geofence radius.
2. **Given** both partners are detected within 100 meters of the same saved place simultaneously for at least 15 minutes, **When** the co-presence threshold is met, **Then** the pet gains `+25 Happiness` and `+40 XP` (maximum once per 24 hours), and both users receive a celebratory notification.

---

### User Story 6 — Home Screen Widget, Memories Archive & Account Settings (Priority: P2)

Users can pin an interactive widget to their device home screen for at-a-glance pet and challenge status, browse past completed photo challenges and streak history, and manage notifications, permissions, and account deletion.

**Why this priority:** Home screen presence drives daily organic re-engagement; memories and settings ensure completeness and store compliance.

**Independent Test:** Can be tested by pinning the home screen widget and verifying it updates when a partner uploads a photo, opening the streak badge to view past memories, and executing unpair/account deletion flows.

**Acceptance Scenarios:**
1. **Given** a user pins the ItsAlive home screen widget (`2x2` or `4x2`), **When** the partner uploads a daily challenge photo or the pet's mood changes, **Then** the widget updates to show the current pet sprite, streak count, and contextual prompt (e.g., *"Partner uploaded! Tap to reveal"* or *"00:42 left for challenge!"*), and tapping it navigates directly to the `Today` screen.
2. **Given** a user taps the streak pill (`🔥 12 Days`) on the `Today` screen or scrolls to Memories in the `Pair` tab, **When** the view opens, **Then** they see a monthly streak calendar and a chronological grid of all past completed dual-photo challenges.
3. **Given** a user opens `Settings` from the profile icon, **When** they review permissions or account options, **Then** they can toggle notification categories, see clear status indicators (`Granted` / `Needs Setup`) for Camera, Notifications, Health, and Location, or permanently delete their account and personal data.

---

### Edge Cases & Failure Modes

- **Partner Leaves Room (Unpairing):** If User A confirms "Leave Room / Unpair", the room is archived immediately, User B receives a push notification and an in-app notice (*"Your partner left the room"*), and both users are cleanly returned to the pairing screen without crashing.
- **Offline Photo Submission:** If a user takes a daily challenge photo while offline, the photo is saved locally, displayed immediately in their slot with an *"Uploading..."* indicator, and automatically uploaded once network connectivity returns. If uploaded before the challenge deadline, it still counts toward the streak.
- **Self-Join & Full-Room Prevention:** If a user enters their own invite code, the app displays *"You cannot join your own room"*. If a third user attempts to join a room that already has 2 members, the app displays *"This room is already full"*.
- **Concurrent Pet Care Actions:** If both partners tap `Feed` or `Play` at the exact same second, energy and happiness values are capped strictly at `100%` without overwriting each other's XP contribution.
- **Inactivity Degradation Rules:** If no activity sync or interaction occurs for 24 hours, `Energy` drops by `-20` and `Happiness` drops by `-15`. If `Energy` or `Happiness` remains at `0` for more than 24 consecutive hours, the pet's mood becomes `SICK` and the streak resets to `0`.
- **Privacy & Location Sanitization:** Captured challenge photos must have embedded location metadata (EXIF GPS) stripped before upload. Raw step records and continuous GPS movement histories are never stored in the shared room or visible to the partner—only daily aggregated totals and shared-place check-in events are synced.

---

## 3. Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Authentication & Session):** The system MUST allow users to sign in via federated social login or email/password, persist active sessions across app restarts, and allow offline access to cached room and pet data.
- **FR-002 (Single Active Room Rule):** The system MUST enforce a strict 1-to-1 room constraint where each user account can belong to at most one active room at any time.
- **FR-003 (Invite Code Generation & Joining):** The system MUST generate a unique 6-digit invite code valid for 24 hours when a user creates a room, and allow a second user to join via manual code entry or deep link.
- **FR-004 (Pet Initialization):** Upon pairing, the system MUST initialize a shared pet (`JointPet`) with the host's chosen egg type (`Green`, `Blue`, `Orange`, `Purple`), a custom name (default `"Buddy"`), `Level 1`, `0 XP`, `80% Energy`, and `80% Happiness`.
- **FR-005 (Daily Challenge Scheduling):** The system MUST trigger one shared daily photo challenge per 24-hour cycle with a synchronized 60-minute active countdown window visible to both partners.
- **FR-006 (Live Camera Capture & Blind Reveal):** The system MUST capture challenge photos exclusively from the live device camera (disallowing old gallery uploads for authenticity) and keep the partner's photo visually blurred until the local user has uploaded their own photo for that day.
- **FR-007 (Partner Nudges):** When one partner has completed the daily challenge and the other has not, the system MUST replace the primary camera button with a "Nudge Partner" action that sends a push notification and enforces a 15-minute cooldown between nudges.
- **FR-008 (Pet Care & Progression Engine):** The system MUST calculate pet stats deterministically: `+10% Energy` & `+15 XP` per feeding (4h cooldown), `+10% Happiness` & `+15 XP` per play session (4h cooldown), `+30 Happiness` & `+50 XP` per dual-completed challenge, and level-up thresholds equal to `currentLevel * 100 XP`.
- **FR-009 (Streak & Grace Window):** The system MUST increment `streakCount` by `1` when both partners complete a daily challenge, place the streak into a 12-hour `AT_RISK` grace window if missed, allow streak recovery via `Save Streak` (`100 XP` or 1 free weekly shield), and reset `streakCount` to `0` if unrecovered after 12 hours.
- **FR-010 (Optional Health Sync):** When health permissions are granted, the system MUST periodically sync daily step counts and sleep minutes, awarding `+25 Energy` / `+20 XP` / `+1 treat` at `8,000 steps` and `+15 Energy` / `+10 XP` at `420 sleep minutes` once per user per day.
- **FR-011 (Optional Shared Places):** When location permissions are granted, the system MUST allow pairs to configure up to 3 shared locations (100m radius) and award `+25 Happiness` / `+40 XP` once per day when both partners are co-present for at least 15 minutes.
- **FR-012 (Home Screen Widget):** The system MUST provide a home screen widget displaying the pet's current visual state, streak count, and challenge status, refreshing automatically when room state changes.
- **FR-013 (Memories & Statistics):** The system MUST preserve all completed dual-photo challenges in a chronological gallery and display a monthly streak calendar and 7/30-day activity summary.
- **FR-014 (Account Deletion & Privacy):** The system MUST provide an in-app flow to leave a room (unpair) and a compliant self-service account deletion flow that permanently removes user profile data, uploaded avatars, and active room membership.

### Key Entities

- **User:** Represents an individual player (`id`, `displayName`, `avatarUrl`, `activeRoomId`, `lastActiveAt`).
- **PairingRoom:** Represents the private 1-on-1 space (`id`, `inviteCode`, `status`, `hostId`, `guestId`, `streakCount`, `streakStatus`, `freeShieldAvailable`, `activeChallengeId`).
- **JointPet:** Represents the couple's shared companion (`name`, `eggType`, `stage`, `level`, `xp`, `energy`, `happiness`, `mood`, `foodInventory`).
- **DailyChallenge:** Represents a single day's shared photo task (`id`, `dayNumber`, `taskDescription`, `hostPhotoUrl`, `guestPhotoUrl`, `expiresAt`, `isCompleted`).
- **DailyActivitySummary:** Represents a user's aggregated daily wellness progress (`date`, `userId`, `stepsCount`, `sleepMinutes`, `stepsGoalRewarded`, `sleepGoalRewarded`).
- **SharedPlace:** Represents a saved meaningful location for a pair (`id`, `roomId`, `title`, `latitude`, `longitude`, `radiusMeters`, `lastJointVisitDate`).

---

## 4. Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001 (Onboarding & Pairing Speed):** 90% of new user pairs complete sign-in, room code sharing, and pet hatching in under **2 minutes** from initial app launch.
- **SC-002 (Daily Challenge Loop Completion):** Users can capture, confirm, and upload a daily challenge photo in under **15 seconds** from tapping the notification or `Take Photo` button, with partner photo unblurring occurring within **3 seconds** of the second upload under standard network conditions.
- **SC-003 (Offline Resilience):** 100% of challenge photos captured while offline are preserved locally without data loss and automatically synchronized within **30 seconds** of network restoration.
- **SC-004 (Permission Graceful Degradation):** 100% of core daily loop flows (pairing, photo challenges, feeding, playing, streak saving) remain completable with zero crashes or blocking dialogs when health and location permissions are denied.
- **SC-005 (Data Consistency):** Concurrent care actions (`Feed`, `Play`, challenge uploads) by both partners result in **0** lost XP updates or out-of-bounds stat values (`< 0` or `> 100`).
- **SC-006 (Widget Freshness):** Home screen widget state reflects partner photo uploads and mood changes within **15 minutes** passively and within **5 seconds** of receiving a push update.