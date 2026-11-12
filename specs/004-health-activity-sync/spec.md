# Feature Specification: Health Activity Sync

**Feature Branch**: `004-health-activity-sync`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Create an isolated specification for feature 004-health-activity-sync, scoped strictly to User Story 4 from the master product specification. This feature depends on User and PairingRoom from 001 and the energy, XP, and food inventory APIs from JointPet in 002. It must detail FR-010 and SC-004 with RFC 2119 wording. The feature covers optional permission requests to read daily steps and sleep duration from the system health store, periodic synchronization into DailyActivitySummary, rewards for 8,000 steps and 420 minutes of sleep, and privacy-safe sharing of only daily totals. It also includes graceful degradation when access is denied and must exclude SDK-specific HealthKit / Health Connect selection, which is reserved for the planning phase."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Optional permission and daily health sync (Priority: P1)

A user who chooses to connect their health data should be able to grant or decline access without blocking the rest of the app. If permission is granted, the app periodically syncs daily aggregates into a room-level health summary and reuses those totals for rewards and display.

**Why this priority**: This is the primary health-loop feature that supports the pair’s shared progression while staying optional and non-disruptive.

**Independent Test**: A user can open the Pair tab, see the health card in an optional state, choose whether to enable access, and if enabled, the app records the daily `stepsCount` and `sleepMinutes` values for reward calculation without exposing raw logs.

**Acceptance Scenarios**:

1. **Given** a user has not granted access to health data, **When** they open the Pair screen, **Then** the health card remains in an optional state and no blocking modal prevents them from continuing to use the app.
2. **Given** the user grants permission to read health data, **When** the app syncs the latest daily aggregates, **Then** the values are stored as `DailyActivitySummary` data for that user and room for the relevant calendar day.
3. **Given** a user has valid permission, **When** the app periodically refreshes history, **Then** the latest `stepsCount` and `sleepMinutes` values are updated without exposing minute-by-minute activity logs to the partner.

---

### User Story 2 - Steps reward and sleep reward (Priority: P1)

A user who meets the daily activity goals should receive a once-per-day reward tied to the app’s shared care loop, and the room should reflect the pair’s combined progress toward the shared target.

**Why this priority**: The rewards create the motivational loop that connects daily wellbeing habits to pet care and progression.

**Independent Test**: A user who reaches 8,000 steps or 420 minutes of sleep in a calendar day receives the matching reward once, while a second reward attempt on the same day is ignored.

**Acceptance Scenarios**:

1. **Given** a user reaches 8,000 steps on a calendar day, **When** the daily health sync runs, **Then** the system marks `stepsGoalRewarded` as true, adds `+25 Energy`, `+20 XP`, and `+1 food treat` exactly once for that day.
2. **Given** a user reaches 420 minutes of sleep on a calendar day, **When** the daily health sync runs, **Then** the system marks `sleepGoalRewarded` as true, adds `+15 Energy` and `+10 XP` exactly once for that day.
3. **Given** a user has already received a reward for a goal on the same calendar day, **When** the sync runs again, **Then** the reward is not awarded again and the daily totals remain stable.

---

### User Story 3 - Privacy-safe pairing display and graceful degradation (Priority: P1)

The room should only show daily aggregate values, while raw activity logs remain private to the individual. If access fails or is denied, the room must continue to function without blocking other core interactions.

**Why this priority**: This preserves user trust, supports privacy, and avoids harm when the optional system integration is unavailable.

**Independent Test**: A user can deny health access and still continue to feed or care for the pet with the health card shown as optional, and the pair never receives raw location or minute-level activity details.

**Acceptance Scenarios**:

1. **Given** a user declines health permissions or the system denies access, **When** they return to the Pair screen, **Then** the health card remains optional and the app continues without blocking the main flow.
2. **Given** a user has connected health data, **When** the app prepares the room summary, **Then** only the daily aggregated totals are transmitted to the pair and no raw minute-level logs are disclosed.
3. **Given** the user cannot or does not want to provide health data, **When** they maintain the pet through feeding or challenge completion, **Then** they can continue to keep the pet energy at 100% without waiting on the health feature.

---

### Edge Cases

- **Given** a user denies health access, **When** the app loads the Pair screen, **Then** the health module shows a non-blocking optional badge and the user remains free to use feeding and challenge features without interruption.
- **Given** the app syncs multiple times in one day, **When** the user already received the step reward, **Then** the system does not grant a duplicate step reward on the same calendar day.
- **Given** the app syncs the room summary, **When** it prepares partner-visible data, **Then** it sends only `stepsCount`, `sleepMinutes`, and the related aggregate status, never raw minute-level logs or location data.
- **Given** a user reaches the goal thresholds only after a later sync, **When** the reward is applied, **Then** it is applied once for that day and the related reward flags are persisted for future syncs.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-010: Health-read permission, daily summary sync, and reward logic**
  - The system MUST offer an optional permission request for reading each user’s daily health aggregates, including steps and sleep duration, without forcing the user to grant permission to continue using the room or pet-care features.
  - The system MUST keep the health card in an optional status when permission has not been granted, denied, unavailable, or revoked.
  - The system MUST synchronize each user’s daily health aggregates into a `DailyActivitySummary` record for the relevant calendar day and pair context.
  - The system MUST periodically refresh the stored daily aggregates for each eligible user so that the latest values are available for reward evaluation and pair display.
  - The system MUST evaluate step and sleep goals against the current calendar day using the synced daily aggregates.
  - The system MUST award `+25 Energy`, `+20 XP`, and `+1 food treat` exactly once per user per calendar day when `stepsCount >= 8000` and `stepsGoalRewarded` is false.
  - The system MUST award `+15 Energy` and `+10 XP` exactly once per user per calendar day when `sleepMinutes >= 420` and `sleepGoalRewarded` is false.
  - The system MUST persist the reward flags `stepsGoalRewarded` and `sleepGoalRewarded` with the daily summary so that repeat syncs do not double-apply rewards in the same calendar day.
  - The system MUST treat the pair’s combined health contribution as a display-only aggregate for the room but MUST NOT expose raw activity logs, minute-by-minute data, or location data to the partner.
  - The system SHOULD continue pet-care and challenge flows without interruption when health data is unavailable or denied.

### Key Entities *(include if feature involves data)*

- **DailyActivitySummary**: Represents the daily health totals for one user within a room on a given calendar day. Key fields include the user reference, room reference, date, `stepsCount`, `sleepMinutes`, `stepsGoalRewarded`, `sleepGoalRewarded`, and `lastSyncedAt`. This feature owns the lifecycle, validation, and privacy rules for this entity.
- **PairingRoom**: Already exists from feature `001-auth-and-pairing`. It provides the room context and pair-level visibility rules for aggregated health data.
- **User**: Already exists from feature `001-auth-and-pairing`. It provides the user identity and daily goal state used to apply and persist health rewards.
- **JointPet**: Already exists from feature `002-pet-care-and-progression`. This feature calls the existing APIs to increase `Energy`, `XP`, and `foodInventory` when the daily step or sleep goals are met.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-004**: At least 95% of users who grant access to daily health data receive correct reward processing for the step and sleep goals and do not receive duplicate rewards within the same calendar day.
- **SC-005**: 100% of room-visible health data is limited to aggregate daily counters and never includes raw per-minute activity logs or location data.
- **SC-006**: 100% of users who deny or lose health access can continue using the app’s care and challenge features without a blocking interruption.
- **SC-007**: 90% of users who enable health sync see the updated daily totals within one refresh cycle after the data becomes available.

## Assumptions

- Health access is optional, not required for the core room and pet-care loop.
- The room can show the pair’s combined daily totals without revealing raw data from the underlying health systems.
- Each user may earn the step and sleep rewards only once per calendar day, even if multiple syncs occur.
- The health feature is isolated from SDK selection and platform-specific implementation details until the planning phase.
- `DailyActivitySummary` is a shared domain entity owned by this feature, while the platform adapters and system integrations are out of scope for this specification.
