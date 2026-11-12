# Feature Specification: Widgets, Memories, and Settings

**Feature Branch**: `006-widgets-memories-settings`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Create an isolated specification for feature 006-widgets-memories-settings, scoped strictly to User Story 6 from the master product specification. This feature consumes data from User, PairingRoom, JointPet, DailyChallenge, and DailyActivitySummary. It must detail FR-012, FR-013, FR-014, and SC-006 in RFC 2119 wording. The feature covers home-screen widgets in 2x2 and 4x2 sizes, memories and statistics access from the streak badge or Pair tab, settings controls for push notification categories and permission status displays, and room exit plus full self-service account deletion. It also includes the edge cases of partner-removal during an active room and offline widget/gallery fallback behavior."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Home widget summary and room updates (Priority: P1)

A user must be able to glance at the home widget and see a current, trusted snapshot of the room state. The widget should refresh on a standard background cadence and immediately after relevant room changes without requiring manual action.

**Why this priority**: The widget is a lightweight entry point that keeps the relationship loop visible even outside the app and directly affects engagement and timely response.

**Independent Test**: A user taps a home widget and lands on `Today`, while the widget updates in the background at least every 15 minutes and within 5 seconds after a room-relevant push update.

**Acceptance Scenarios**:

1. **Given** there is an active room and a valid pet state, **When** the home widget is displayed, **Then** it shows the current pet sprite, the streak count, and the latest challenge-state text such as `Partner uploaded! Tap to reveal` or `00:42 left for challenge!`.
2. **Given** the widget is visible in the background, **When** 15 minutes pass without a manual refresh, **Then** the widget refreshes to show the latest room or pet state.
3. **Given** a room-related push update is received, **When** the data is processed, **Then** the widget refreshes within 5 seconds and opens the `Today` screen when tapped.

---

### User Story 2 - Memories and statistics access (Priority: P1)

A user should be able to inspect the room’s past progress and the current streak timeline from the most natural entry points, without losing context or needing to search through other screens.

**Why this priority**: The memories and statistics view brings transparency and motivation into the shared routine by preserving the relationship history that drives long-term retention.

**Independent Test**: A user taps the streak badge or opens the Pair tab, then sees a monthly streak calendar, 7/30 day activity summaries, and a chronological grid of all completed pair photo challenges.

**Acceptance Scenarios**:

1. **Given** a user sees the streak badge on the `Today` screen or Pair tab, **When** they tap it, **Then** they open the `Memories & Statistics` view.
2. **Given** the memories view is open, **When** the user reviews the room history, **Then** they see the monthly streak calendar, 7/30 day activity summaries, and completed challenge entries in chronological order.
3. **Given** a room has no completed challenge history, **When** the user opens the memories view, **Then** the experience remains usable and clearly indicates there is no historical data yet.

---

### User Story 3 - Settings, permissions, and account lifecycle (Priority: P1)

A user must be able to manage notification preferences and see the status of system permissions, while also being able to safely leave the room or delete the account with a complete self-service flow.

**Why this priority**: These settings and account actions affect trust, privacy, and the ability to recover from transitions in the relationship lifecycle.

**Independent Test**: A user can change push categories, confirm permission statuses, leave the room, or delete the account and complete the flow with the required partner notification and data cleanup.

**Acceptance Scenarios**:

1. **Given** a user opens the settings screen, **When** they review notification options, **Then** they can switch push notification categories on or off according to their preference.
2. **Given** a user opens the permissions section, **When** they check system permission status, **Then** the app shows `Granted` or `Needs Setup` for Camera, Notifications, Health, and Location.
3. **Given** a user chooses to leave the room, **When** the unpair action is confirmed, **Then** the room is archived and the partner is notified before the user is returned to the pairing flow.
4. **Given** a user chooses to delete the account, **When** the self-service deletion flow is confirmed, **Then** the profile, uploaded avatars, personal photos, and room membership are fully removed and the partner is notified before the account record is removed.

---

### Edge Cases

- **Given** a user deletes their account while the room is still active, **When** the process runs, **Then** the partner receives the same partner-left-room notification and the room is severed before the deleted user record is fully removed.
- **Given** the app is offline, **When** the widget or memories view is requested, **Then** the UI shows the last cached pet or photo state instead of failing entirely.
- **Given** the widget receives a room push update, **When** the update is processed, **Then** the widget refreshes within 5 seconds and the tap target still opens `Today`.
- **Given** there are no recorded historical challenge entries, **When** the memories section loads, **Then** it still displays the remaining controls and a clear empty-state message without crashing.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-012: Home widget visibility and refresh behavior**
  - The system MUST present the pet sprite, the current streak count, and the active challenge status in both `2x2` and `4x2` home-screen widget variants.
  - The system MUST update the widget state at least every 15 minutes while the app is in the background.
  - The system MUST refresh the widget within 5 seconds after receiving a room-related push notification or other room-state change.
  - The system MUST open the `Today` screen when the user taps the widget.
  - The system SHOULD keep the widget content aligned with the latest cached room state even if the app is not actively open.

- **FR-013: Memories and statistics access**
  - The system MUST allow a user to open `Memories & Statistics` from the streak badge (`🔥 X Days`) or from the Pair tab.
  - The system MUST display a monthly streak calendar for the room.
  - The system MUST present a summary of the room’s 7-day and 30-day activity trends.
  - The system MUST show a chronological grid of all completed pair photo challenge entries, including the associated day markers and challenge history.
  - The system SHOULD present a clear empty state when no historical challenge entries exist.

- **FR-014: Settings, notification categories, and account control**
  - The system MUST allow users to manage push-notification categories in settings.
  - The system MUST display a `Granted` or `Needs Setup` status for Camera, Notifications, Health, and Location permissions.
  - The system MUST provide a self-service `Leave Room / Unpair` flow that removes the current user from the active room and notifies the remaining partner before returning the user to the pairing flow.
  - The system MUST provide a full self-service account deletion flow that clears the profile, uploaded avatar assets, personal photos, and room membership without leaving the account in a partially active state.
  - The system MUST notify the partner before the account is fully removed when the active room is still present, in a manner consistent with the room-leave flow.
  - The system SHOULD preserve the user’s ability to continue using the app after a room change or account-deletion confirmation until the final removal is complete.

### Key Entities *(include if feature involves data)*

- **User**: Existing user account and profile data used for identity, settings, notifications, and deletion flows.
- **PairingRoom**: Existing room entity used for unpair, room-status updates, and partner notification flow.
- **JointPet**: Existing pet state used for widget sprite and status display.
- **DailyChallenge**: Existing challenge entity used for widget challenge state and the memories grid.
- **DailyActivitySummary**: Existing daily health and activity summary used for 7-day and 30-day activity trends.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-006**: At least 95% of users who open the widget, streak badge, or settings view can complete the intended action within one interaction and without encountering a crash or broken state.
- **SC-007**: 100% of room-visible data shown in the widget and memories area is derived from cached or current room state, and no stale or invalid data is presented after a room change.
- **SC-008**: 100% of self-service account deletion and room-leave flows notify the partner and remove the room membership before the account record is fully cleared.
- **SC-009**: 90% of users can access saved challenge history and activity summaries in offline mode using the most recently cached state, without losing their progress context.

## Assumptions

- Home-screen widget behavior is treated as a product feature separate from app UI implementation details.
- The widget and gallery are consumers of existing room, pet, challenge, and activity data rather than owners of that data.
- Account deletion and room leave are full lifecycle actions that must notify the remaining partner and clean all user-owned content before final removal.
- Offline fallback uses the last cached state for the widget and gallery without requiring live connectivity.
- The permission statuses shown in settings are user-facing summaries and do not imply a specific platform SDK implementation.
