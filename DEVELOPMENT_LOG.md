# ItsAlive Development Log

**Coverage:** repository history through 2026-11-21  
**Purpose:** a human-readable record of implemented work and documented follow-ups.

## How to read this log

This history is reconstructed from Git commits, the checked-in feature specifications/tasks, and available agent-session notes. Git commit metadata identifies the repository committer, not necessarily the individual agent that performed the work; this log therefore records repository changes rather than assigning individual authorship. A feature specification describes intended work and is not, by itself, evidence that the feature has been implemented.

## Development timeline

### 2026-11-17 to 2026-11-19 — Authentication and session lifecycle

- **Authentication design and planning (`0ed2cb2`, 2026-11-17):** added the authentication feature specification, plan, data model, contracts, quickstart and requirements checklist; updated the project constitution and MCP configuration.
- **Authentication/session refactor (`6ae15b7`, 2026-11-18):** moved the auth repository contract into the auth domain; clarified provider-authentication and profile/session states; revised Firebase auth result/error handling; removed app-managed Firebase-token persistence while preserving legacy-token cleanup on logout. Added profile validation and coverage for auth use cases, view models, logout, session/cache behavior and onboarding.
- **Existing-profile onboarding and session tests (`14eca7e`, 2026-11-19):** onboarding now loads an existing profile, and session repository behavior received focused tests.
- **Navigation tests (`728bf36`, 2026-11-19):** added app-boundary authentication navigation coverage.
- **Password recovery (`82aa41e`, 2026-11-19):** added provider-backed password recovery and tests for a neutral confirmation outcome and failure cases.

The authentication task file reports the applicable implementation and focused test work complete for registration, sign-in, Google sign-in, password recovery, profile completion/validation, session handling, logout, navigation, and safe error handling. It also records test/build checks as passing during that implementation work.

### Current Copilot CLI implementation session — authentication UI and network follow-up

- **Authentication UI:** added reusable form fields, primary buttons and password-strength meter; decomposed the start, sign-in and sign-up screens into smaller composables; connected Google sign-in and password-recovery navigation.
- **Recovery and validation:** added a recovery bottom sheet/ViewModel with neutral confirmation, sanitized failures and a 60-second retry cooldown; added password-strength state/calculation and enforced privacy acceptance before sign-up.
- **Network safety:** wrapped Firestore operations to preserve coroutine cancellation and convert provider failures to a safe typed error; made profile writes await completion and tested FCM request construction. Configured explicit 15-second OkHttp timeouts. HTTP logging is disabled in all builds because the Backendless credential is part of the URL; sensitive-header redaction remains configured.
- **Tests and validation:** added auth/network unit tests and Compose instrumentation tests. `:feature:auth:testDebugUnitTest`, `:core:network:testDebugUnitTest`, `:feature:auth:assembleDebugAndroidTest`, and `:app:assembleDebug` passed together. Instrumentation tests compiled but were not run without an available device/emulator.
- **Evidence limits:** network tests cover the extracted profile decode/missing-profile helpers, not a Firestore-backed `DocumentSnapshot.toObject` fixture, so T045 remains unchecked. The password tier mapping was not specified by the task and should be confirmed against product expectations. UI followed written requirements and existing theme because verified pixel-level Figma context was unavailable. Work remains uncommitted.

**Still unresolved in the checked-in authentication record:**

- T031: the human security review required by the project constitution has not been recorded.
- T032: server-side session/refresh-token revocation remains blocked until an approved backend contract and implementation owner/path are supplied.
- T045: see the current implementation entry above; provider-backed Firestore deserialization is not covered.
- The task file says T026 is checked, but its dependency notes explicitly say protected-route gating and `MainViewModel` routing remain incomplete because destination and session/profile semantics are unspecified. Treat this item as **not fully verified** until that contradiction is resolved.
- T001 is still unchecked; several policy/configuration decisions are listed as unconfirmed in the task file. Do not infer backend or product behavior from the presence of SDK wiring.

### 2026-11-12 — Product definition, development workflow and Today screen

- **Product and feature specifications (`7cc63e9`):** documented the master product concept and feature specifications/checklists for pet progression, authentication/pairing, daily photo challenges, health/activity sync, shared places/geofencing, and widgets/memories/settings.
- **Spec-Kit setup (`5d96e04`):** added repository specification templates, PowerShell workflow scripts, project constitution, skills/workflows and ignore rules.
- **Life-Sync planning/configuration (`16bde96`):** added a Life-Sync artifact, repository guidance and Gradle daemon JVM configuration.
- **Today screen and theme (`879dde6`):** redesigned the main screen around a Today view, added challenge/quick-visit UI components, and updated app theme/colors.

The health sync, geofencing and widget feature documents are planning artifacts; this history does not establish that those integrations have been implemented.

### 2026-10-24 to 2026-11-12 — Pet progression and reminders

- **Pet dashboard (`ecd697f`, 2026-10-28):** introduced the pet dashboard screen, view model/state/events, repository and local-data access, plus pet resource configuration and initial stage artwork.
- **Pet stages and room context (`53af3a2`, 2026-10-28):** passed room identity through the dashboard flow and added additional pet-stage/color artwork.
- **Streak expiration (`a2370e4`, 2026-11-12):** added expiration scheduling/receiver behavior and updated room/pet persistence and repository mapping to support the changed challenge/streak model.

### 2026-10-16 to 2026-10-25 — Challenges, photos, notifications and data flow

- **Challenges and home UI (`87db3bd`, 2026-10-19):** added challenge functionality and revised the main screen.
- **Challenge synchronization fixes (`2fad7a3`, `9f732c4`, 2026-10-21):** corrected challenge download behavior.
- **Challenge photo upload (`3bb81f9`, 2026-10-22):** added photo upload for challenges.
- **Notifications (`5f21f9e`, `ab0cd84`, 2026-10-24):** added notification support and push-notification sending.
- **Offline-first and follow-up fixes (`33abdb2`, `83f8b8e`, `03a65c5`, 2026-10-25):** refactored challenge data access toward offline-first behavior, cleaned the remote challenge source, and fixed navigation/cache access and room observation after task submission.

### 2026-10-07 to 2026-10-16 — Profile, pairing and module boundaries

- **Account/profile groundwork (`9e298f6`, `5122005`, `3b97d77`, `98ad8f5`, 2026-10-07 to 2026-10-12):** added room/avatar data to the user, profile UI, avatar upload/customization and logout from the profile screen; fixed sign-up error display and local theme application.
- **Pairing and room flows (`cfc8aac`, `be052e6`, `675f7f4`, 2026-10-07 to 2026-10-14):** added room creation/hosting and joining flows, choice dialog, waiting-for-friend UI, and related navigation/database fixes.
- **Architecture refactor (`b2d9719`, `3fe9106`, `897cd5b`, `a21417c`, `f36ca5d`, 2026-10-14 to 2026-10-16):** reorganized the multi-module structure and moved auth/shared/local data responsibilities behind narrower module boundaries; made inner data layers internal and corrected dependencies.

### 2026-06-01 to 2026-06-21 — Application foundation

- **Initial app and authentication (`2ec754f` through `8547d8f`, 2026-06-01 to 2026-06-04):** created the Android project and initial start, sign-up and sign-in screens; established navigation, shared UI elements, network/data modules and Firebase authentication.
- **Auth completion (`545e67c`, `97a162b`, `282896b`, 2026-06-04 to 2026-06-06):** added email/password or Google auth paths, dependency fixes and input validation.
- **Onboarding, domain and pairing (`73a56e5` through `1c3ec91`, 2026-06-16 to 2026-06-17):** added onboarding/navigation, initial pairing/main-screen work, and domain-module foundations.
- **Main screen (`b64b43d`, `b8f4425`, 2026-06-21):** fixed onboarding completion navigation and implemented the initial main-screen UI.

## Current project picture

The repository has grown from an authentication/onboarding prototype into a multi-module Android app with profile and room/pairing flows, challenge/photo functionality, notifications, pet progression/dashboard UI, and a redesigned Today screen. Authentication and session behavior received a substantial specification-led implementation and test pass in November.

The product specification also describes future integrations (including Health Connect, shared-location/geofencing and widgets). Check their feature modules and implementation status before treating those roadmap items as delivered. For the most precise authentication task-by-task status, consult `specs/001-authentication/tasks.md`; this log intentionally summarizes rather than replacing that record.

## Source references

- Git history: commits from `2ec754f` (2026-06-01) through `82aa41e` (2026-11-19).
- `specs/001-authentication/tasks.md` and the other files under `specs/`.
- `itsalive_master_product_specification.md` and `README.md`.
