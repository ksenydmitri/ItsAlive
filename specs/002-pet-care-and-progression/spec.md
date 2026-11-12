# Feature Specification: Pet Care and Progression

**Feature Branch**: `002-pet-care-and-progression`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Create an isolated specification for feature 002-pet-care-and-progression, scoped strictly to User Story 3 from the master product specification. This feature depends on the completed User, PairingRoom, and base JointPet initialization already delivered in 001-auth-and-pairing. It must detail FR-008, the grace window and streak recovery part of FR-009, and SC-004 and SC-005. It must cover growth stages (BABY, TEEN, ADULT), XP progress bar logic, mood states (HAPPY, HUNGRY, SLEEPY, LONELY, SICK), Energy and Happiness bars, Feed (+10% Energy, +15 XP, 1 food consumed, 4h cooldown), Play (+10% Happiness, +15 XP, 4h cooldown), and Save Streak logic (AT_RISK 12-hour grace window, 100 XP or free weekly shield, reset to 0 after 12 hours). The feature owns the JointPet stat, level, and mood business logic and provides internal contracts used by later features 003, 004, and 005. It must include edge cases for simultaneous Feed/Play actions and inactivity degradation, but must not include camera, photo challenges, steps/sleep, or geofence logic."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View pet progression and mood state (Priority: P1)

A user who has entered a valid room and initialized their shared pet must be able to understand the pet's current growth stage, mood, energy, happiness, and XP progress without ambiguity.

**Why this priority**: This is the foundation of the care loop. The user must understand the pet's state before deciding to feed or play.

**Independent Test**: A user opens the pet room and can verify each visible stat, stage badge, and mood state against the current values in the shared pet model.

**Acceptance Scenarios**:

1. **Given** a newly initialized `JointPet` with `Level 1`, `0 XP`, `80% Energy`, and `80% Happiness`, **When** the pet room loads, **Then** the UI shows the current stage as `BABY`, the level as `Lvl 1`, the XP progress bar, and the mood badge based on active status rules.
2. **Given** a pet with `Level 3`, **When** the room is displayed, **Then** the stage badge changes to `TEEN` and the progress formula reflects the current level's total XP threshold.
3. **Given** a pet with `Level 7`, **When** the room is displayed, **Then** the stage badge changes to `ADULT` and the mood, energy, and happiness bars remain bounded between `0%` and `100%`.
4. **Given** a pet with `energy < 30`, **When** the system evaluates mood, **Then** the pet must show `HUNGRY`.
5. **Given** a pet with `energy == 0` or `happiness == 0` for longer than 24 hours, **When** the system evaluates mood, **Then** the pet must show `SICK`.

---

### User Story 2 - Feed and play actions update pet stats and XP (Priority: P1)

A user must be able to care for the pet through valid actions that increase energy or happiness while also rewarding meaningful progression.

**Why this priority**: Feeding and play are the main direct feedback loops in the care experience and the source of persistent collaboration and progression.

**Independent Test**: A user triggers `Feed` and `Play` actions from the pet room and verifies that XP and state values update exactly as specified and never exceed their caps.

**Acceptance Scenarios**:

1. **Given** a user has at least one food item in `foodInventory` and the feed cooldown is clear, **When** they perform `Feed`, **Then** the pet's `Energy` increases by `+10%` up to `100%`, the pet gains `+15 XP`, and the food inventory is reduced by `1`.
2. **Given** a user triggers `Play` after the cooldown window is clear, **When** the interaction completes successfully, **Then** the pet's `Happiness` increases by `+10%` up to `100%` and the pet gains `+15 XP`.
3. **Given** a pet is already at `100% Energy`, **When** a user triggers `Feed`, **Then** the energy value remains at `100%` and no extra value is applied beyond the cap.
4. **Given** a pet is already at `100% Happiness`, **When** a user triggers `Play`, **Then** the happiness value remains at `100%` and no extra value is applied beyond the cap.

---

### User Story 3 - Save streak during grace period and recover from inactivity (Priority: P1)

A user must be able to preserve a streak during the 12-hour `AT_RISK` grace window and understand how the pet degrades when it remains unattended.

**Why this priority**: This protects the daily ritual and ensures the system behaves predictably under missed days and stalled care.

**Independent Test**: A streak is marked `AT_RISK`, the user saves it using either XP or a free shield, and a missed session falls into the inactivity rule without leaving invalid state.

**Acceptance Scenarios**:

1. **Given** the streak transitions to `AT_RISK` within the 12-hour grace window, **When** the user taps `Save Streak`, **Then** the app must either consume `100 XP` or use one free weekly shield if available and restore the streak without resetting the count.
2. **Given** the streak remains unresolved for 12 hours after entering `AT_RISK`, **When** the grace window expires, **Then** the system resets `streakCount` to `0`.
3. **Given** the pet is inactive for 24 hours, **When** the inactivity rule is applied, **Then** `Energy` decreases by `-20` and `Happiness` decreases by `-15`.
4. **Given** `Energy` or `Happiness` remains at `0` for 24 consecutive hours, **When** the inactivity check runs, **Then** the pet is marked `SICK` and the streak count is reset to `0`.

---

### Edge Cases

- **Given** both partners trigger `Feed` or `Play` in the same second, **When** the actions are processed concurrently, **Then** the total stat increase must never exceed the cap of `100%` and no XP must be lost.
- **Given** the pet has been inactive for 24 hours, **When** the scheduler applies the degradation rule, **Then** `Energy` and `Happiness` must be reduced deterministically without creating negative or out-of-range values.
- **Given** a streak is `AT_RISK`, **When** the user does not consume a shield or pay the XP recovery cost before the 12-hour window ends, **Then** `streakCount` must reset to `0` exactly at the end of the grace period.
- **Given** the pet's mood is evaluated, **When** the state is still `HAPPY` but `Energy < 30`, **Then** the system must prioritize `HUNGRY` when that rule applies.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-008: Pet state calculation, stage display, and stat bounds**
  - The system MUST calculate and display the pet's growth stage as `BABY` for `Level 1–2`, `TEEN` for `Level 3–6`, and `ADULT` for `Level 7+`.
  - The system MUST calculate and display the XP progress bar as `currentXP / currentLevel * 100` and MUST ensure the formula is applied using the active level value, not a static historical value.
  - The system MUST display and maintain the pet mood in one of the valid states: `HAPPY`, `HUNGRY`, `SLEEPY`, `LONELY`, or `SICK`.
  - The system MUST show `Energy` and `Happiness` on a normalized scale from `0%` to `100%` inclusive.
  - The system MUST clamp all stat modifications so that `Energy` never drops below `0%` or exceeds `100%`, and `Happiness` must follow the same bounded rules.
  - The system MUST show `HAPPY` when `Energy >= 50` and `Happiness >= 50`, unless another state rule has higher priority such as `HUNGRY`, `SLEEPY`, `LONELY`, or `SICK`.
  - The system MUST treat `SICK` as the highest-priority condition when the pet has `Energy == 0` or `Happiness == 0` for more than 24 hours.

- **FR-009: Feed and play actions**
  - The system MUST reduce `foodInventory` by `1` when a `Feed` action succeeds.
  - The system MUST increase `Energy` by `+10%` when a valid `Feed` action is processed, capped at `100%`.
  - The system MUST increase `XP` by `+15` when a valid `Feed` action is processed.
  - The system MUST apply a 4-hour cooldown to each successful `Feed` action, either individually or as a shared cooldown when the paired context requires a single shared timer.
  - The system MUST increase `Happiness` by `+10%` when a valid `Play` action is processed, capped at `100%`.
  - The system MUST increase `XP` by `+15` when a valid `Play` action is processed.
  - The system MUST apply a 4-hour cooldown to each successful `Play` action, either individually or as a shared cooldown when the paired context requires a single shared timer.
  - The system MUST reject or ignore duplicate `Feed` or `Play` actions that occur within the active cooldown window so the action cannot be double-applied.
  - The system SHOULD allow both partners to trigger their own care actions independently while still enforcing the cap and XP safety rules.

- **FR-010: Save Streak grace window and recovery**
  - The system MUST enter a 12-hour `AT_RISK` grace window when the active streak is missed and no valid completion has occurred within the normal period.
  - The system MUST allow `Save Streak` only while the streak is in the active `AT_RISK` grace window.
  - The system MUST permit a streak save by spending either `100 XP` or one available free weekly shield when `freeShieldAvailable` is `true`.
  - The system MUST restore the streak count and remove the `AT_RISK` state when a valid save is applied.
  - The system MUST reset `streakCount` to `0` when the 12-hour grace period expires without any successful save.
  - The system MUST prevent a second save after the expiration time from restoring a stale or already-reset streak.

- **FR-011: Inactivity degradation and sickness**
  - The system MUST reduce `Energy` by `-20` and `Happiness` by `-15` after 24 hours of inactivity.
  - The system MUST evaluate inactivity degradation deterministically from the last recorded care or sync timestamp, without relying on UI state alone.
  - The system MUST mark the pet `SICK` when `Energy == 0` or `Happiness == 0` remains true for more than 24 consecutive hours.
  - The system MUST reset `streakCount` to `0` when the pet remains in a zero-stat sickness condition for more than 24 consecutive hours.
  - The system MUST ensure that inactivity degradation never produces a negative value below `0%` or a value above `100%` after a stat update.

### Key Entities *(include if feature involves data)*

- **User**: Already exists from feature `001-auth-and-pairing`. This feature depends on the user's identity and room membership context for care actions and streak recovery decisions.
- **PairingRoom**: Already exists from feature `001-auth-and-pairing`. It provides the shared room state, active streak status, and the room-level lock for care interaction validity.
- **JointPet**: This feature is the owner of the business logic for stat changes, level progression, and mood evaluation of the shared pet. It exposes internal contracts for XP, Energy, and Happiness updates that subsequent features `003`, `004`, and `005` will consume.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-004**: 100% of concurrent `Feed` and `Play` actions that occur within the same second must preserve XP and keep `Energy` and `Happiness` within the `0%–100%` range.
- **SC-005**: 100% of streaks entering the `AT_RISK` window must either be saved within 12 hours or reset to `0` deterministically at expiration, with no stale recovery attempt restoring an already expired streak.
- **SC-001**: At least 95% of users can identify the correct pet stage, mood, and XP progress from the pet room without confusion during their first session.
- **SC-002**: At least 90% of care actions successfully increase the correct stat and XP value on the first attempt when the cooldown window is clear.
- **SC-003**: At least 99% of stat updates remain bounded within valid ranges after 24-hour inactivity degradation, feed, or play actions.

## Assumptions

- The identity, active room, and initial pet creation logic are already implemented by the preceding `001-auth-and-pairing` feature.
- The feature scope is intentionally limited to pet care, progression, mood evaluation, and streak protection; it does not include photo challenges, camera workflows, steps/sleep tracking, or geofence features.
- `Feed` and `Play` actions operate based on a single shared room state and may be triggered by either partner, but not by any third party.
- `freeShieldAvailable` is a room or user-level property that can be checked during the `AT_RISK` recovery flow.
- The `XP` and stat formulas are deterministic and must remain consistent regardless of action ordering when care actions are processed at nearly the same time.
