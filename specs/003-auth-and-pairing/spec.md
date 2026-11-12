# Feature Specification: Auth and Pairing

**Feature Branch**: `003-auth-and-pairing`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Isolated specification for feature 001-auth-and-pairing, scoped to User Story 1 only. Includes the following requirements: displayName validation, session persistence and offline room access, one-user-one-active-room rule, unique 6-digit room code generation with 24h TTL and deep-link join flow, egg color selection, synchronized hatching flow, default pet naming, and initialization of JointPet; explicit handle for self-join, full room, and partner-unpair edge cases; excludes feeding, daily challenges, health, geolocation, and account deletion."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Complete onboarding and secure session setup (Priority: P1)

A user who launches the app for the first time must be able to create an account, set a valid profile name, and continue without losing access to a cached room when the network is unavailable.

**Why this priority**: This is the minimum path required before any pairing or room experience can begin. It establishes identity, profile validation, and resilience for returning users.

**Independent Test**: A user can start from a fresh install, sign in or create an account, enter a valid display name, and continue into pairing. The same flow must work when the app is reopened offline with a cached active room.

**Acceptance Scenarios**:

1. **Given** a new user without a saved session, **When** they authenticate successfully and enter a name with 2–24 valid characters, **Then** the app stores the profile and continues to the room setup flow.
2. **Given** a user whose name contains only spaces, unsupported symbols, or fewer than 2 characters, **When** they attempt to continue, **Then** the app blocks submission and instructs them to correct the profile name.
3. **Given** a user already authenticated and previously cached in the local store, **When** they reopen the app with no network access and an active room exists, **Then** the app must load the cached room state and allow entry to the main flow without requiring a network round trip.

---

### User Story 2 - Create a room, invite a partner, and prevent invalid joins (Priority: P1)

A user must be able to create a room and invite a partner without creating duplicate active rooms or allowing invalid joins.

**Why this priority**: The room is the foundation for the pair relationship and must be unique, valid, and safe against collisions or misuse.

**Independent Test**: A user can generate a room code, share it via invite link, and the system rejects attempts to join their own room or a room that is already full.

**Acceptance Scenarios**:

1. **Given** a user without an active room, **When** they create a room, **Then** the app generates a unique 6-digit code that remains valid for 24 hours and offers copy/share actions.
2. **Given** a user opens a deep link such as `itsalive://join/482910`, **When** the app loads, **Then** the join screen is prefilled with that code and the user can continue without manual entry.
3. **Given** a user tries to join a room they already created, **When** they enter their own invitation code, **Then** the app shows the error message "You cannot join your own room" and blocks entry.
4. **Given** a user attempts to join a room whose capacity is already full, **When** they submit the code, **Then** the app shows the error message "This room is already full" and keeps them on the join screen.

---

### User Story 3 - Complete pairing and initialize the shared pet (Priority: P1)

Once a second partner joins, the system must synchronize both users into the hatching flow and establish the shared pet state before the app proceeds.

**Why this priority**: Pairing creates the shared identity and the initial pet state; without a synchronized setup, the experience cannot proceed safely.

**Independent Test**: Two users can complete the room flow, select matching egg and pet naming settings, and both land on the same final hatching step with a valid initial pet state.

**Acceptance Scenarios**:

1. **Given** a room with one active user and a second partner joins, **When** the room state updates to a valid pair, **Then** both users are moved to the Step 02 / 02 hatching flow at the same time.
2. **Given** a user has selected an egg color from `Green`, `Blue`, `Orange`, or `Purple`, **When** they continue to the hatching step, **Then** the selected egg and pairing state are preserved for both partners.
3. **Given** no pet name is supplied in the hatching step, **When** the user continues, **Then** the system applies the default name "Buddy".
4. **Given** the pair is confirmed, **When** the shared pet is initialized, **Then** the initial `JointPet` state is `Level 1`, `0 XP`, `80% Energy`, and `80% Happiness`.
5. **Given** a partner leaves the room after a valid pair is active, **When** the room is archived, **Then** the remaining user receives the message "Your partner left the room" as both an in-app notification and a push, and both users are returned to the pairing flow without a crash.

---

### Edge Cases

- **Given** a user enters a code for a room they created, **When** the app validates the join attempt, **Then** it must reject the submission with the message "You cannot join your own room" and keep the user in the safe state.
- **Given** a user enters the code for a room already containing two members, **When** the app validates the join request, **Then** it must reject the attempt with the message "This room is already full" and prevent any state mutation.
- **Given** a partner leaves the room after pairing is active, **When** the room is archived, **Then** the remaining partner receives both an in-app notification and push message reading "Your partner left the room" and is returned to the pairing flow without a crash.
- **Given** a user reopens the app with no connectivity, **When** the app restores authentication and room state, **Then** it must rely on locally cached identity and room metadata to maintain a valid offline-first user experience.
- **Given** a profile name is blank or invalid, **When** the user attempts to continue, **Then** the app must block progression until the user provides a valid display name.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001: User identity and profile validation**
  - The system MUST require a valid `displayName` for all new users before the onboarding flow can complete.
  - The system MUST allow a `displayName` length from 2 to 24 characters inclusive.
  - The system MUST reject `displayName` values that are empty, whitespace-only, shorter than 2 characters, or longer than 24 characters.
  - The system MUST reject `displayName` values containing unsupported special characters, and SHOULD provide a clear inline validation message that explains the required format.
  - The system MUST persist the authenticated session so that the user remains recognized across app restarts.
  - The system SHOULD allow offline access to locally cached user and room data when connectivity is unavailable during return visits.
  - The system MUST recover to a valid state when a cached room exists without creating duplicate room records or session inconsistencies.

- **FR-002: One-user-one-active-room rule**
  - The system MUST ensure each user has at most one active room at a time.
  - The system MUST prevent a user from creating a second active room while they already belong to an active pairing room.
  - The system MUST prevent a user from joining a room while they are already assigned to another active room.
  - The system SHOULD surface a clear, user-facing error message when the user attempts a disallowed room transition.
  - The system MUST maintain the active-room relationship as a single source of truth for room membership and navigation state.

- **FR-003: Room code generation, lifecycle, and invite flow**
  - The system MUST generate a unique 6-digit room code for each new room.
  - The system MUST ensure the generated room code is unique within the active room registry before the room becomes joinable.
  - The system MUST assign a 24-hour TTL to each room and MUST invalidate or archive a room when that window expires.
  - The system MUST allow the user to copy the room code to the clipboard.
  - The system MUST allow the user to share a deep link that opens the app and navigates to the join flow.
  - The system MUST detect and prefill the room code when the app is opened through a valid `itsalive://join/<code>` deep link.
  - The system SHOULD preserve the share/copy action state across re-entry to the room setup screen without requiring a new room generation.

- **FR-004: Pairing and pet initialization**
  - The system MUST let the user choose an egg color from `Green`, `Blue`, `Orange`, and `Purple`.
  - The system MUST preserve the selected egg choice in the room state before the user proceeds.
  - The system MUST synchronize the transition of both partners to the Step 02 / 02 hatching screen when the second partner joins the room.
  - The system MUST allow the user to name the pet during hatching, and MUST default the pet name to `Buddy` when the user leaves it blank or unedited.
  - The system MUST create a base `JointPet` instance during successful pairing initialization.
  - The system MUST initialize the `JointPet` with `Level = 1`, `XP = 0`, `Energy = 80%`, and `Happiness = 80%`.
  - The system SHOULD keep both partners aligned on the same room and pet state while the room is active.

### Key Entities *(include if feature involves data)*

- **User**: Represents the authenticated person in the app. Key attributes include identity, profile name, avatar state, active room reference, and persisted session state. Owner: `:feature:auth` and `:feature:pairing` together define the user contract and lifecycle for this feature.
- **PairingRoom**: Represents a room that may hold one current pair and one shared pet setup. Key attributes include a unique 6-digit code, 24-hour lifetime, active membership, shared egg choice, and joinability state. Owner: `:feature:pairing` defines the room lifecycle and validation rules for this feature.
- **JointPet**: Represents the shared companion created after both partners are matched. Key attributes include the initial evolution state, XP, energy, happiness, and default naming. Owner: `:feature:personalisation` defines the baseline constructor and its initial values for this feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least 90% of first-time users can complete profile setup and reach the pairing screen within 5 minutes without assistance.
- **SC-002**: At least 99% of room creation and join attempts are accepted or rejected with the correct user-facing message and no duplicate room state.
- **SC-003**: At least 95% of users who encounter an invalid join or invalid profile name can correct the issue and continue without leaving the flow.
- **SC-004**: 100% of self-join, full-room, and partner-unpair edge cases must surface a clear message and return the user to a valid state without crashing the app.

## Assumptions

- Users are expected to have valid sign-in credentials through the app’s supported auth methods and may resume sessions after temporary loss of connectivity.
- A user can only belong to one active room in this feature scope; multi-room support is outside the scope of User Story 1.
- Room and user records remain locally cached for offline-first access when the app is reopened without internet connectivity.
- The feature does not cover later lifecycle tasks such as feeding, daily challenges, geolocation enforcement, or account deletion; those are intentionally deferred to future feature branches.
