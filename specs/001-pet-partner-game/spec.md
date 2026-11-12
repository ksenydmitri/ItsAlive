# Feature Specification: ItsAlive Pet Partnership Game

**Feature Branch**: `001-pet-partner-game`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "Build ItsAlive from scratch as a companion app where two partners share one pet, complete daily challenges together, maintain streaks, and care for a virtual companion through shared habits, feedback loops, and room-based collaboration. The experience includes onboarding, pairing, daily challenge flow, pet care, shared-place check-ins, settings, notifications, widgets, and edge-case handling while preserving a clear product narrative and reliable game progression."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Start a room and begin the shared journey (Priority: P1)

As a new user, I want to create or join a pairing room and choose a pet egg so that I can begin a shared routine with a partner and establish the foundation for my companion journey.

**Why this priority**: Room creation is the entry point for every core product loop; without a valid pair and pet, the rest of the game cannot start.

**Independent Test**: A user can open the app, create or join a room, choose an egg, name the pet, and complete initial setup without assistance.

**Acceptance Scenarios**:

1. **Given** a user with no active room, **When** they create a room and choose an egg, **Then** a room is created with a valid invite code and the user begins setup for a shared companion.
2. **Given** a user with a valid room code, **When** they join an existing room, **Then** they are placed into the shared setup flow and can continue to the first challenge without duplicate or conflicting room state.
3. **Given** a new pair has completed setup, **When** they enter the app, **Then** they see the daily challenge flow and can begin caring for their pet together.

---

### User Story 2 - Care for a pet through daily habits and shared actions (Priority: P1)

As a partner in a room, I want to complete a daily challenge, care for my pet, and maintain energy and happiness so that the companion grows, remains healthy, and the shared routine feels rewarding.

**Why this priority**: The product's primary engagement loop is daily participation, habit maintenance, and pet progression.

**Independent Test**: A user can complete a challenge, feed or play with the pet, and observe changes in the pet health, mood, and level progression without relying on any other app features.

**Acceptance Scenarios**:

1. **Given** a user with a current daily challenge, **When** they complete the challenge with their partner or upload the required photo evidence, **Then** the pet gains happiness and experience and the streak advances according to the room rules.
2. **Given** a pet whose energy or happiness is low, **When** the user feeds, plays, or completes partner actions, **Then** the values recover within the defined limits and the pet mood reflects the improvement.
3. **Given** a pet at a lower stage of development, **When** it accumulates enough experience, **Then** it advances to the next growth stage and the user can see the updated status in the pet room.

---

### User Story 3 - Maintain momentum, memory, and connection over time (Priority: P2)

As a user in a long-term partnership, I want reminders, streak protection, shared places, and memory snapshots so that the habit remains engaging and I can keep the relationship and routine alive even during busy or low-energy days.

**Why this priority**: Retention depends on resilience under missed days, low engagement, and contextual constraints, not only on the initial onboarding flow.

**Independent Test**: A user can receive a reminder, recover from a streak risk state, browse memories, and understand their current room status without needing a support agent.

**Acceptance Scenarios**:

1. **Given** a room approaching an expiration or streak risk state, **When** the user sees a reminder or active warning, **Then** they understand the next action required and can choose to protect the streak or complete the challenge.
2. **Given** a user with existing room history, **When** they open the memories or pair profile, **Then** they can review prior daily completions and shared moments in a clear timeline.
3. **Given** a user who loses connectivity or is temporarily unavailable, **When** they later return to the app, **Then** the system preserves room state and the pending daily challenge remains understandable and recoverable.

---

### User Story 4 - Keep the app useful outside the main loop (Priority: P3)

As a user, I want settings, permissions, widgets, and background reminders to work reliably so that the app stays useful when I am not actively on the main screen.

**Why this priority**: These capabilities improve trust and long-term engagement but are not necessary for the minimal viable product to function.

**Independent Test**: A user can review permissions, manage notifications, pin a widget, and access account controls without encountering blocked or confusing flows.

**Acceptance Scenarios**:

1. **Given** a user who wants quick access to their pet status, **When** they add a home screen widget, **Then** they can view key information without opening the app.
2. **Given** a user needs to grant camera, notifications, or location access, **When** they open the permissions hub, **Then** they can see the current status and complete the required setup in a clear flow.
3. **Given** a user wants to leave or delete a room, **When** they select the account or room action, **Then** they see a clear confirmation path and the app updates their state consistently.

### Edge Cases

- A user opens the app without an internet connection and there is no cached room state; the app presents a clear retry path instead of blocking progression.
- A user receives a duplicate or invalid room code; the system prevents entry and provides a clear explanation without creating conflicting room membership.
- A pet has no hatch time or an incomplete state during early onboarding; it remains in a safe default state rather than inventing invalid values.
- A room expires or reaches a streak-risk boundary exactly at a timer threshold; time handling preserves the existing semantics and prevents negative remaining time.
- One partner uploads a photo but the other does not; the challenge state reflects the partial completion without misreporting the room as fully complete.
- A user attempts to feed or play multiple times in a narrow time frame; repeated triggering is dampened and the state remains stable.
- A user denies camera, health, or location permissions; the app remains usable and surfaces non-blocking status prompts instead of locking the experience.
- A user leaves a room or deletes an account; their partner receives a clear notification and the app returns to a valid onboarding or room-creation state.
- Time zones differ between partners; the app still interprets room timers and daily challenge windows consistently in a shared reference timeline.
- A photo upload is interrupted; the app keeps the pending context and retries when connectivity returns.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to create a room, join an existing room, and complete the onboarding sequence that establishes a pet and a daily challenge.
- **FR-002**: The app MUST provide a daily challenge loop that captures partner participation, expected activity, and daily reward outcomes in a clear and consistent sequence.
- **FR-003**: The pet MUST have health and mood metrics that degrade over time and can recover through core actions such as feeding, playing, sleep, and daily challenge completion.
- **FR-004**: The app MUST support pet progression through experience thresholds and clearly visible life stages, from early growth through adulthood.
- **FR-005**: The app MUST support streak progression, risk warnings, and a protected recovery action when a streak is about to expire.
- **FR-006**: The app MUST represent partner activity, room status, and progress using a shared view of the room that remains understandable to both participants.
- **FR-007**: The app MUST support shared place check-ins, reminders, and challenge completion feedback that encourage paired participation without requiring forced in-app activity at all times.
- **FR-008**: The app MUST provide a settings flow, permissions hub, account controls, and support for persistent reminders or widgets without making the core loop inaccessible.
- **FR-009**: The app MUST preserve user safety and privacy by handling camera, location, and account data in a transparent and non-destructive way.
- **FR-010**: The system MUST handle missing connectivity, repeated input, and stalled uploads gracefully while preserving a consistent user state.
- **FR-011**: The feature scope MUST include room setup, pet care, daily challenge routines, pair and memory management, and supporting background reminders and widgets, but it MUST NOT expand into unrelated product domains.
- **FR-012**: The system MUST support a consistent product experience across the core user journey: onboarding, daily challenge completion, pet care, room continuity, and account management.

### Key Entities *(include if feature involves data)*

- **PairingRoom**: The shared room that connects two users and governs their active daily challenge, streak, and room lifecycle.
- **JointPet**: The companion associated with the room, including growth stage, health, mood, level, and experience.
- **DailyChallenge**: The active daily objective that both partners can complete, with timers, completion states, and shared reward rules.
- **PetStage**: The growth classification used to communicate the pet's development from early stage to adult stage.
- **PetMood**: The emotional or health state derived from core resource metrics and room activity.
- **RoomStatus**: The current room lifecycle state, including waiting, active, at-risk, and archived or resolved states.
- **UserProfile**: The user identity and display information used in pairing, room state, and account lifecycle.
- **MemoryEntry**: A shared activity record or challenge moment that can be revisited in a gallery or history view.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user can complete onboarding, room pairing, and first pet setup in under 10 minutes without support assistance.
- **SC-002**: At least 90% of users who begin a room can reach the first daily challenge and complete the first core engagement flow on their first visit.
- **SC-003**: Daily challenge completion and pet care actions lead to a visible improvement in pet energy, happiness, and progression for the majority of active users within a normal usage week.
- **SC-004**: Users can recover from short-term interruptions, missed actions, and low-connectivity states without losing the local meaning of their room or pet progress.
- **SC-005**: At least 95% of challenge and room state transitions are presented to users in a clear, consistent status with no ambiguous or contradictory system messages.
- **SC-006**: Users can review their shared history and room health information in a way that makes both accountability and progress understandable without requiring technical support.
- **SC-007**: The app supports persistent reminders, widget visibility, and account-level controls that are accessible while preserving the core flow for users who do not need advanced settings.

## Assumptions

- Active room creation and daily challenge participation are the primary product value drivers in the initial release.
- Users are expected to be paired with one consistent partner rather than managing multiple simultaneous room contexts.
- The product is designed for a mobile-first experience with clear recovery from connectivity gaps and low-activity windows.
- Shared room state, pet progression, and daily challenge updates are treated as the core product truth, with local persistence used to support resilience and offline recovery.
- The initial release is focused on the core relationship loop and companion-care gameplay, not on a large-scale social network or broader lifestyle platform.
- Default privacy and permission flows prioritize user safety and transparent explanation over frictionless but opaque data collection.
