# Feature Specification: Shared Places Geofencing

**Feature Branch**: `005-shared-places-geofencing`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Create an isolated specification for feature 005-shared-places-geofencing, scoped strictly to User Story 5 from the master product specification. This feature depends on PairingRoom from 001 and the Happiness and XP APIs from JointPet in 002. It must detail FR-011 and SC-004 in RFC 2119 wording. The feature covers creation, viewing, and deletion of SharedPlace entries on the Pair tab, a maximum of 3 saved places per room, geofence-based co-presence detection for a 100-meter radius for at least 15 minutes, a reward of +25 Happiness and +40 XP with a 24-hour cooldown via lastJointVisitDate, and privacy-safe handling that stores only enter/exit event facts without historical coordinates or partner live location. It also covers the optional non-blocking behavior when background location permission is denied."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Shared place management in the Pair tab (Priority: P1)

A couple in an active room must be able to create, review, and remove a small set of meaningful shared places, while the system keeps the room clean and within the supported limit of three saved places.

**Why this priority**: Shared places are the room’s social anchor and the basis for co-presence detection; the creation flow must remain simple and bounded.

**Independent Test**: A paired user can create a place by selecting a location on a map, naming it, and saving it as one of up to three room-level places, without exceeding the cap or storing unnecessary location history.

**Acceptance Scenarios**:

1. **Given** a valid active room, **When** a user opens the Pair tab, **Then** they can create a `SharedPlace` with a map-selected point, a name, and a fixed 100-meter geofence radius.
2. **Given** a room already has three saved places, **When** the user attempts to add another one, **Then** the app blocks the action and shows a clear limit message informing the user that a maximum of three places per room is allowed.
3. **Given** a saved shared place exists, **When** the user views the Pair tab, **Then** they can see the place name and relevant summary without exposing the partner’s live location.
4. **Given** a shared place is no longer relevant, **When** the user removes it, **Then** the place is deleted from the room and no longer contributes to co-presence detection.

---

### User Story 2 - Co-presence reward detection and cooldown (Priority: P1)

When both partners are simultaneously inside the same 100-meter zone for at least 15 continuous minutes, the system may award a shared visit bonus once per 24-hour period and notify both members of the pair.

**Why this priority**: This is the reward loop that turns real-life togetherness into pet progression and reinforces meaningful shared moments.

**Independent Test**: Two users simultaneously remain within a room’s 100-meter radius for at least 15 minutes, and the system awards +25 Happiness and +40 XP once for that period, with a 24-hour cooldown preventing duplicate rewards.

**Acceptance Scenarios**:

1. **Given** both partners are within the same `SharedPlace` geofence for at least 15 continuous minutes, **When** the co-presence check runs, **Then** the system awards `+25 Happiness` and `+40 XP` to the pet for that visit.
2. **Given** the reward was already granted within the last 24 hours, **When** both partners stay inside the same place again, **Then** no additional reward is granted until the cooldown period expires.
3. **Given** a co-presence reward is granted, **When** the system completes the reward processing, **Then** both partners receive a festive push notification indicating the shared visit was recognized.

---

### User Story 3 - Privacy-safe geofencing and degraded access (Priority: P1)

The app must enforce strict privacy boundaries for shared place data and remain usable even when background location permission is unavailable or denied.

**Why this priority**: The feature adds real-world location awareness, which must be optional, privacy-preserving, and non-disruptive to the rest of the experience.

**Independent Test**: A user can deny background location permission and continue using the rest of the app normally, while the room stores only zone-entry and zone-exit facts, not raw location traces or the partner’s live position.

**Acceptance Scenarios**:

1. **Given** background location permission is denied, **When** the user opens the app, **Then** the rest of the app remains fully usable and the user is not shown a blocking modal demanding continued permission.
2. **Given** geofencing is active and a user enters or exits a 100-meter zone, **When** the room records the event, **Then** it stores only the zone event fact and the associated timestamp, never the full raw path or continuous GPS stream.
3. **Given** a room summary is shown to the partner, **When** it displays the shared place status, **Then** it does not reveal the partner’s live coordinates or a live map of their current location.

---

### Edge Cases

- **Given** the room already contains three saved places, **When** a user tries to create a fourth, **Then** the system rejects the new place and the room remains at the configured maximum.
- **Given** both partners stay within the same geofence for 15 minutes but the reward cooldown has not expired, **When** the evaluation runs, **Then** no duplicate reward is given and the system keeps the last reward window intact.
- **Given** a user denies background location access, **When** the app continues running, **Then** the rest of the room, pet care, and challenge features remain available without intrusive permission prompts.
- **Given** a user’s location data is processed for co-presence detection, **When** the system stores room-level data, **Then** only entry/exit facts are retained and continuous GPS coordinates are not persisted or shown.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-011: Shared place creation, validation, co-presence detection, and reward rules**
  - The system MUST allow a user in an active room to create a `SharedPlace` from the Pair tab by selecting a location on a map, entering a place name, and storing a fixed geofence radius of 100 meters.
  - The system MUST allow a saved `SharedPlace` to be viewed and removed by authorized users in the same room.
  - The system MUST enforce a hard maximum of 3 saved `SharedPlace` entries per `PairingRoom` and MUST reject attempts to create a fourth saved place.
  - The system MUST detect co-presence when both partners are simultaneously inside the same `SharedPlace` geofence and remain continuously within that 100-meter zone for at least 15 minutes.
  - The system MUST award `+25 Happiness` and `+40 XP` to the shared pet exactly once per qualifying visit when co-presence is maintained for at least 15 continuous minutes.
  - The system MUST enforce a 24-hour cooldown for shared-visit rewards using `lastJointVisitDate` and MUST prevent a new reward from being granted until the cooldown window has expired.
  - The system MUST send a celebratory push notification to both partners when a qualifying co-presence reward is granted.
  - The system MUST maintain the reward and process only for the active room and must not apply the reward to unrelated pairs or locations.
  - The system MUST preserve privacy by recording only the fact of zone entry and exit for each `SharedPlace` event and MUST NOT store raw movement history or continuous GPS coordinates in the room data model.
  - The system SHOULD allow the rest of the app to function normally when background location permission is denied or unavailable.

### Key Entities *(include if feature involves data)*

- **SharedPlace**: Represents a room-level place with a user-defined name, a selected map point, a fixed 100-meter geofence radius, and a room association. This feature owns the schema, validation, storage, and lifecycle of this entity.
- **PairingRoom**: Already exists from feature `001-auth-and-pairing` and provides the room context for shared-place limits and room-scoped co-presence detection.
- **JointPet**: Already exists from feature `002-pet-care-and-progression` and exposes the `Happiness` and `XP` reward APIs used by this feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-004**: At least 95% of qualifying co-presence windows that last 15 minutes or more result in the correct reward and notification, while no duplicate reward is granted within the 24-hour cooldown window.
- **SC-005**: 100% of room-level location records are limited to geofence entry/exit facts and do not include raw track history or continuous coordinate streams.
- **SC-006**: 100% of users who decline background location permission can continue using the rest of the application without a blocking interruption.
- **SC-007**: 100% of rooms enforce the limit of no more than three saved `SharedPlace` entries at any time.

## Assumptions

- The room can contain up to three named shared places and no more than that.
- Co-presence is evaluated only for the same room and same `SharedPlace` geofence, not across unrelated rooms.
- The system uses a 24-hour cooldown based on a date value (`lastJointVisitDate`) to prevent reward spamming.
- The app keeps only event-level facts such as entry and exit timestamps for geofence evaluation; raw location history is not persisted or shared.
- Background location permission is optional and must not block unrelated app functionality.
