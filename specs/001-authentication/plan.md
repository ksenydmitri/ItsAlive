# Implementation Plan: Authentication

**Branch**: `001-authentication`  
**Date**: 2026-11-17  
**Spec**: [spec.md](spec.md)  
**Repository baseline**: `879dde6`

## Summary

Implement authentication in the existing Kotlin/Android multi-module application, using Firebase as the observed authentication integration. Keep provider identity/session state separate from profile state. Personalisation owns profile completion; Shared owns common contracts; App coordinates navigation.

Implement only methods and behaviors confirmed by product/backend configuration. Missing decisions are blockers, not defaults to infer from existing code.

## Approved requirements

The following profile rules are approved and must not be treated as open decisions:

- **`name`**: required; 2–30 Unicode code points after NFC normalization, trimming, and collapsing consecutive whitespace. Supports Unicode letters and single spaces, hyphens, and straight apostrophes between letters. Rejects digits, emoji, control characters, typographic apostrophes, and other symbols.
- **`nickName`**: required and accepted as a login identifier; 2–24 characters excluding `@`; Latin letters, digits, `_`, and `.` only. Normalize and store in lowercase; enforce case-insensitive uniqueness atomically at the authoritative persistence boundary. Display `@` as a prefix; do not store or count it.
- Existing usernames that fail the new format remain valid for sign-in and are not forcibly changed.

Validate at both the UI and authoritative persistence boundary. Client validation is not a security or data-integrity guarantee.

## Decisions that remain gated

Confirm from approved product/backend configuration before implementing dependent behavior:

- enabled authentication methods and provider-specific requirements;
- whether display name is required during sign-up or can be completed later;
- verification, recovery, account linking, rate limits, and approved user-facing copy;
- session, revocation, concurrent-session, and offline-access policies;
- authenticated destination;
- whether an approved non-Firebase consumer requires a separately persisted Firebase ID token.

Do not infer these from the current UI, SDKs, or repository structure.

## Technical context and boundaries

- **Platform**: Kotlin/Android; existing multi-module Gradle project, JVM target 11 in the inspected feature modules.
- **Observed dependencies**: Compose, AndroidX lifecycle/navigation, coroutines/Flow, Hilt, Firebase Auth/Firestore, Credential Manager, Room, and DataStore. Keep existing versions; this plan does not authorize dependency or provider-console changes.
- **Session authority**: Firebase Auth state is the provider-session source of truth. A cached profile alone MUST NOT establish authentication.
- **Token handling**: A Google ID token is input to Firebase credential exchange, not a Firebase session token. Obtain Firebase ID tokens from the SDK only when required. Research found no observed consumer of the additional encrypted token cache; verify consumers before changing or removing it.
- **Module ownership**: `:core:network` handles provider integration; `:core:local` handles local persistence; `:shared` owns common domain contracts; `:feature:auth` owns authentication UI/flows; `:feature:personalisation` owns profile completion; `:app` coordinates navigation. Confirm actual code ownership before edits; avoid feature-to-feature dependency cycles.
- **Scope**: Authentication only. Do not implement pairing, rooms, pets, account deletion, or their navigation semantics.
- Do not inspect or expose secret configuration.

## Design constraints

1. Keep provider authentication state distinct from profile availability/completion.
2. Do not report authentication, registration, verification, or profile completion as successful before the relevant provider or persistence operation confirms success.
3. Preserve coroutine cancellation; do not convert it to success or a provider failure.
4. Handle duplicate submissions, uncertain retries, partial profile-save failure, stale responses, concurrent refresh, logout, and account switching without creating conflicting or cross-account state.
5. Remove local authenticated access on logout even if offline or remote sign-out fails. Implement server-side revocation only when an approved provider/backend contract requires it.
6. Apply offline behavior only according to the approved offline policy. A cached profile does not prove a current provider session.
7. Keep navigation neutral until the authenticated destination is approved.

## Project documentation

```text
specs/001-authentication/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── feature-contracts.md
```

`tasks.md` is produced by the later task-generation step.

The source code remains in the existing `app/`, `feature/`, `shared/`, and `core/` modules. Tests belong in the relevant modules’ existing test source sets. Do not add a test framework or module unless repository inspection shows it is necessary and the change is explicitly justified.

## Validation

Use commands in [quickstart.md](quickstart.md). Tests should cover the applicable approved behavior, including provider success/failure, cancellation, profile-save retry, session restoration, offline policy, logout, stale responses, account switching, safe errors, and profile validation/uniqueness.

Separate unit/build evidence from Firebase integration and device-level evidence. Do not claim tests or builds passed unless they were run. Do not test an unapproved policy as if it were settled.

## Constitution check

**Planning: PASS. Implementation: gated by applicable decisions above.**

Preserve privacy and module boundaries; do not expose credentials or secrets; verify failure, cancellation, retry, concurrency, and account-isolation behavior. Obtain the qualified human security review required by `.specify/memory/constitution.md` CONST-42 before merge.

No Constitution exception, additional module, or quantitative performance target is proposed.