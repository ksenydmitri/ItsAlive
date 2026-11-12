# Feature Specification: Daily Photo Challenge

**Feature Branch**: `003-daily-photo-challenge`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Create an isolated specification for feature 003-daily-photo-challenge, scoped strictly to User Story 2 from the master product specification. This feature depends on PairingRoom from 001 and the pet care progression engine from 002. It must detail FR-005, FR-006, FR-007, and the streak increment part of FR-009, and SC-002 and SC-003. It covers one 24-hour challenge cycle with a synchronized 60-minute countdown, strict live-camera-only capture with EXIF GPS stripping, blind reveal behavior, the Nudge Partner action with 15-minute cooldown, and the transition to AT_RISK when no pair completes the challenge in time. It owns the DailyChallenge entity and must avoid memories, streak calendars, and widgets."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Daily challenge schedule and timer synchronization (Priority: P1)

A pair in an active room must receive exactly one daily challenge per 24-hour cycle and see the same synchronized 60-minute countdown regardless of device timezone or local clock drift.

**Why this priority**: The challenge timer is the shared daily rhythm that drives the whole relationship loop; if it drifts, trust and streak fairness break down.

**Independent Test**: A room with two users can start a challenge at the same logical time and both users see the same countdown and completion state from their devices.

**Acceptance Scenarios**:

1. **Given** a valid active room with both partners present, **When** the challenge window opens, **Then** the app creates exactly one `DailyChallenge` for the current 24-hour cycle and shows a single synchronized 60-minute countdown to both partners.
2. **Given** a challenge is active and the timer is running, **When** either partner opens the `Today` screen, **Then** the same remaining time is displayed to both and the challenge cannot be duplicated for the same period.
3. **Given** the challenge has not been completed by both partners before expiration, **When** the 60-minute countdown ends, **Then** the room transitions to the `AT_RISK` streak state and the challenge is closed.

---

### User Story 2 - Camera-only capture and blind reveal (Priority: P1)

A user must capture a challenge photo only from the live device camera and the system must reveal the partner's photo only after the current user has uploaded their own photo, keeping the partner's image blurred until that moment.

**Why this priority**: Photo authenticity and privacy are core to trust in the joint daily challenge experience.

**Independent Test**: A user can open the camera, capture a new photo, confirm it, receive a blurred partner slot until their own upload is confirmed, and then see both photos unblur after the second upload within the required time window.

**Acceptance Scenarios**:

1. **Given** a user opens the challenge flow, **When** they attempt to select a source photo, **Then** only the live camera is permitted and gallery imports are blocked.
2. **Given** a user captures a live photo and confirms the upload, **When** the file is sent, **Then** the system strips EXIF GPS metadata before upload and preserves the image in the user’s slot with a clear "Uploading..." state until transmission completes.
3. **Given** User A has uploaded their photo and User B has not yet uploaded, **When** User B opens the challenge screen, **Then** User A's photo is shown blurred until User B confirms their own upload.
4. **Given** both partners have completed their upload before the timer expires, **When** the second upload finishes, **Then** both photos unblur for both parties within 3 seconds and the system may apply the shared completion reward to the pet and streak.

---

### User Story 3 - Nudge partner while challenge is unresolved (Priority: P1)

After a user has uploaded their own photo, the app must support an explicit reminder action that informs the partner and enforces a short cooldown to prevent spam.

**Why this priority**: This keeps the challenge moving even when one partner is delayed and protects the sense of fairness and urgency.

**Independent Test**: A user sees the camera button replaced by "Nudge Partner" after upload, triggers it, receives a confirmation state, and cannot trigger the same nudge again until the cooldown ends.

**Acceptance Scenarios**:

1. **Given** a user has already uploaded their current-day photo, **When** the challenge remains incomplete because the partner has not yet uploaded, **Then** the primary action becomes "Nudge Partner" instead of reopening the camera.
2. **Given** a user taps "Nudge Partner", **When** the system triggers the reminder, **Then** a push notification is sent to the partner and the action enters a 15-minute cooldown.
3. **Given** the nudge action is in cooldown, **When** the same user attempts to trigger it again, **Then** the action is blocked and the UI indicates that the reminder is temporarily unavailable.

---

### Edge Cases

- **Given** a user captures a photo while offline, **When** the system saves it locally and displays it as "Uploading...", **Then** the photo must be transmitted automatically when connectivity returns within 30 seconds and still count toward the current challenge if captured before the timer expires.
- **Given** both partners fail to upload within the 60-minute challenge window, **When** the timer reaches zero, **Then** the room is marked `AT_RISK` and control passes to the pet care and streak engine for recovery handling.
- **Given** the second photo upload completes, **When** both photos are revealed, **Then** the pet gains `+30 Happiness` and `+50 XP`, and the `streakCount` increases by `+1`.
- **Given** a user attempts to upload an image from the gallery instead of the device camera, **When** the selection is processed, **Then** it must be rejected and the user remains in the live-camera flow.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-005: Daily challenge lifecycle and timer synchronization**
  - The system MUST create exactly one `DailyChallenge` per 24-hour cycle for a valid active pair.
  - The system MUST apply a single synchronized 60-minute countdown to both partners for the active `DailyChallenge`.
  - The system MUST ensure both partners view the same remaining challenge time and completion state during the active window.
  - The system MUST close the active challenge when the countdown reaches zero and MUST transfer the pair to the `AT_RISK` streak state if both photos were not uploaded before expiration.
  - The system SHOULD maintain a consistent challenge identifier across the pair and MUST prevent duplicate challenge creation within the same 24-hour cycle.

- **FR-006: Camera-only photo capture and EXIF sanitization**
  - The system MUST capture challenge photos exclusively from the live device camera and MUST forbid gallery-based or file-picker imports for this feature.
  - The system MUST strip EXIF GPS metadata from the uploaded image before the file is transmitted or stored as a challenge asset.
  - The system MUST ensure the user-facing upload flow remains in the camera capture path until the photo is accepted.
  - The system MUST reject non-live-camera inputs without allowing the user to continue the challenge flow with a gallery image.
  - The system SHOULD preserve the user’s privacy by removing location metadata before a completed photo is sent to any remote storage.

- **FR-007: Blind reveal, completion rewards, and partner nudges**
  - The system MUST display each partner’s uploaded photo as blurred to the other partner until the current user has confirmed their own upload for the active challenge.
  - The system MUST reveal both photos to both partners within 3 seconds after the second valid photo upload is accepted.
  - The system MUST increment `streakCount` by `+1` when both partner photos are accepted before the challenge expires.
  - The system MUST increase the shared pet's `Happiness` by `+30` and `XP` by `+50` when both partner photos are accepted and the challenge is completed successfully.
  - The system MUST replace the camera button with a `Nudge Partner` action after the user has uploaded their own photo but before the challenge is complete.
  - The system MUST send a push notification to the partner when `Nudge Partner` is triggered.
  - The system MUST apply a 15-minute cooldown to each `Nudge Partner` action and MUST prevent repeated nudges from re-triggering before the cooldown expires.

- **FR-009A: Streak completion and risk transition**
  - The system MUST increment `streakCount` by `+1` when both partners fully complete the active challenge before the 60-minute timer ends.
  - The system MUST transition the streak to `AT_RISK` when the challenge expires without both partners uploading a valid photo.
  - The system MUST transfer control to the streak recovery and grace-window logic defined in the pet care progression feature after the `AT_RISK` transition is triggered.

### Key Entities *(include if feature involves data)*

- **DailyChallenge**: Represents the single challenge instance for a room during a 24-hour cycle. Key attributes include the room reference, challenge day, prompt text, both photo slots, expiration timestamp, completion state, and acceptance status. Owner: `:feature:daily-photo-challenge` defines the lifecycle for this challenge entity and its acceptance rules.
- **PairingRoom**: Already exists from feature `001-auth-and-pairing`. It supplies the room state, active challenge association, and streak status used by this feature.
- **JointPet**: Already exists and is managed by feature `002-pet-care-and-progression`. This feature triggers the shared pet reward updates for completed challenge actions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-002**: 95% of challenge sessions complete with both users seeing the same timer and a successful upload/reveal sequence when both partners finish within the 60-minute window.
- **SC-003**: 100% of challenge uploads that occur while offline are preserved locally and uploaded automatically once connectivity is restored, with no more than 30 seconds delay after the network becomes available.
- **SC-001**: At least 90% of users can begin, capture, confirm, and complete a daily challenge within 15 seconds after tapping the action button when both partners are ready.
- **SC-004**: 100% of challenge expirations without two valid photos must move the streak to `AT_RISK` instead of silently continuing the previous streak state.

## Assumptions

- The active room, user identity, and shared pet progression logic already exist from features `001` and `002`.
- The challenge is strictly pair-based and does not include group rooms, memory archive views, or widget interactions.
- Photo validity requires a live capture from the device camera; gallery uploads are intentionally excluded from this feature scope.
- The app may temporarily keep an offline upload in local storage while the user is disconnected and will retry automatically once connectivity returns.
- The streak risk-transfer occurs after the challenge deadline and is handled by the progression feature's grace-window rules.
