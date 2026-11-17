# Implementation Plan: Authentication

**Branch**: `001-authentication` | **Date**: 2026-11-17 | **Spec**: [spec.md](spec.md)

**Input**: Draft feature specification at `specs/001-authentication/spec.md`; repository baseline `879dde6`.

**Repository context**: The current Git branch is `ai-specification`. The SpecKit setup script selected `specs/001-auth/`, which has no matching specification. This plan is placed beside the actual standalone specification at `specs/001-authentication/`.

## Summary

Plan the Authentication lifecycle using the existing Kotlin/Android modular application and Firebase integration as the implementation baseline. Keep provider identity and Firebase-managed authentication-session behavior separate from profile completion; profile creation, validation, and editing belong to Personalisation, with common domain models and repository contracts in Shared. Preserve the existing feature/module boundaries and use Firebase SDK session state rather than treating an app-cached profile or a separately persisted ID token as proof of authentication.

The specification is Draft. Its Section 12 product decisions—including enabled methods, profile policy, verification/recovery, session and offline policy, and authenticated destination—remain explicit implementation gates. No product decision or unapproved display-name/username rule is resolved by this plan.

## Technical Context

**Language/Version**: Kotlin for Android; repository modules use Android Gradle Plugin and JVM target 11 for the inspected feature modules.

**Primary Dependencies**: Jetpack Compose, AndroidX lifecycle/navigation, Kotlin coroutines and Flow, Hilt, Firebase Authentication/Firestore, Credential Manager for the observed Google flow, Room, and DataStore. Keep to dependencies already present; dependency versions and provider-console configuration are not changed by this plan.

**Storage**: Firebase Authentication SDK-managed session; Firestore remote profile; Room local profile cache; DataStore currently contains an additional encrypted token value. Research found no observed consumer requiring that duplicate token cache. Retain no app-managed credential copy unless an approved non-Firebase consumer is identified.

**Testing**: Existing Gradle unit/instrumented test tasks, with focused tests to be added for presentation/domain behavior, persistence mappings, session restoration, error/cancellation paths, and auth integration. The inspected Auth-related tests are template examples, not acceptance coverage.

**Target Platform**: Android; use the repository's configured SDK levels and supported devices.

**Project Type**: Multi-module Android application.

**Performance Goals**: Immediate local validation feedback and visible loading are required. No quantitative latency target is specified; do not invent one.

**Constraints**: Authentication must remain independent of pairing/rooms/pets. Do not read or expose secret configuration. Firebase provider state and local profile state must remain distinct. Client checks do not replace authoritative provider/backend checks. Preserve coroutine cancellation and explicitly handle retries, races, partial failures, logout, and account switching.

**Scale/Scope**: One Android application and its participating feature/core modules. User volume, device concurrency targets, and rollout scale are not specified.

### Product decisions that gate implementation

These are not technical defaults and must not be inferred from the current code:

- Confirm enabled sign-in methods, identifier and password policy, verification applicability, recovery support, account-linking policy, rate limits, approved copy, and neutral authenticated destination.
- Reconcile and approve the Draft display-name/username policy, including whether display name is required during sign-up, allowed characters, Unicode normalization/counting, and atomic username uniqueness.
- Approve session/revocation/concurrent-session behavior and offline access policy.
- Confirm whether any approved non-Firebase backend consumes a separately persisted Firebase ID token.

The plan can define boundaries and validation evidence without resolving these decisions. Implementation tasks that require a decision above must remain blocked until an accountable product/backend owner resolves it or marks it not applicable.

## Constitution Check

**Gate before Phase 0 — PASS for planning; implementation remains gated.**

- **Security and human control (CONST-01/03/05/06):** no secrets or provider-console configuration are inspected; unresolved material product choices are recorded, not selected.
- **Evidence and scope (CONST-04/09/36):** distinguish observed code from target behavior; exclude rooms, pairing, pets, and account deletion.
- **Privacy and credentials (CONST-13/14/16):** never log credentials/tokens; keep provider SDK credentials behind infrastructure boundaries; verify account isolation and cleanup.
- **Architecture (CONST-32/33):** follow the existing module structure; place provider integration in `:core:network`, local persistence in `:core:local`, common domain contracts in `:shared`, auth UI/session flow in `:feature:auth`, profile ownership in `:feature:personalisation`, and composition/navigation in `:app`. This is a target model, not an assertion about current code.
- **Failures and concurrency (CONST-34):** cancellation, retries, stale responses, partial persistence, offline behavior, and logout races require explicit outcomes.
- **Verification (CONST-37–42):** test failure and boundary paths, report only executed checks, and obtain qualified human review before merging authentication/security changes (CONST-42).

**Gate after Phase 1 — PASS FOR DESIGN; IMPLEMENTATION BLOCKED:** the proposed design preserves the Constitution. Product decisions above remain prerequisites for implementation and acceptance; they are not silently converted into defaults.

## Project Structure

### Documentation (this feature)

```text
specs/001-authentication/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── feature-contracts.md   # Existing source-backed contract analysis
```

`tasks.md` is not created by `/speckit-plan`; it belongs to the later task-generation phase.

### Source Code (repository root)

```text
app/
feature/
├── auth/
├── personalisation/
└── home/                       # Existing logout caller; not an auth owner
shared/
core/
├── network/
└── local/
```

Relevant tests are colocated under each module's `src/test` and `src/androidTest` source sets. Pairing and pet modules are excluded from this feature's implementation scope.

**Structure Decision**: Retain the current multi-module Android structure. Authentication remains responsible for provider identity/session operations; Personalisation owns profile creation, validation, and editing; Shared owns common domain models and repository contracts; core modules provide infrastructure adapters; App coordinates top-level routing. Avoid feature-to-feature cycles and keep app navigation neutral until the destination decision is approved.

## Design Decisions

1. **Session authority**: Treat Firebase Authentication's current user/auth-state listener as the provider-session source of truth. A cached profile can support permitted offline presentation but cannot establish authentication on its own.
2. **Token handling**: Treat the Credential Manager Google ID token as input to Firebase credential exchange, not as the Firebase session token. Obtain Firebase ID tokens from the Firebase SDK only when a consumer requires one. The SDK manages its native session/refresh lifecycle; do not independently persist duplicate Firebase ID tokens by default.
3. **Profile boundary**: Keep profile completion state distinct from provider authentication state. The app may route an authenticated identity to Personalisation when profile completion is required; the final neutral destination remains a product decision.
4. **Validation authority**: Implement client feedback and authoritative profile persistence checks only after the Draft name/username policy is approved. Username uniqueness requires an atomic authoritative persistence contract, not a client-only check.
5. **Error and cancellation behavior**: Map provider failures to safe domain/presentation outcomes; propagate coroutine cancellation; do not turn cancellation into success or a generic failure. Protect session state from overlapping or stale requests.
6. **Persistence and logout**: Preserve identity-scoped local profile data only according to the approved offline/data-isolation policy. Local access must be removed even when provider revocation is unavailable; server-side revocation semantics remain provider-specific and require approval.

## Validation Strategy

Use the runnable commands and acceptance scenarios in [quickstart.md](quickstart.md). Verification must distinguish unit/build evidence from Firebase/provider integration and device-level behavior. The minimum design coverage includes token exchange, success and failure outcomes, cancellation, missing profiles, offline restoration, logout failure/offline/repetition, stale responses, profile persistence retry, account switching, safe errors, and the still-unapproved username uniqueness rule.

No tests or builds were run while authoring this documentation plan.

## Complexity Tracking

No Constitution exception or additional project/module is proposed.
