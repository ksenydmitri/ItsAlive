# Tasks: Authentication

**Input**: `specs/001-authentication/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/feature-contracts.md`, `quickstart.md`

## Rules for implementation

- Implement only authentication methods and behaviors confirmed by existing product/backend configuration. If required configuration is missing, report a blocker; do not choose a default.
- `name` and `nickName` validation rules in `spec.md` §9.2 are approved. `nickName` is a login identifier.
- Existing usernames that violate the new format remain valid for sign-in and MUST NOT be forcibly changed.
- Do not add pairing, room, invitation, or pet behavior.
- Keep task statuses and record only changes and test results actually verified.

## Phase 1: Foundation

- [ ] **T001** Confirm or locate the configuration needed by dependent tasks: enabled auth methods, authoritative profile-persistence contract, profile-completion timing, verification/recovery, session/offline behavior, token consumers, and authenticated destination. Do not reopen approved profile rules or legacy-username compatibility.
- [x] **T002 [P]** Move the existing `AuthRepository` interface to `feature/auth/src/main/java/com/furiqd/auth/domain/repository/AuthRepository.kt`; update its implementation and use cases. Do not create a duplicate API.
- [x] **T003 [P]** Define provider-authentication and profile-availability state types and repository contracts in `shared/src/main/java/com/furiqd/shared/domain/model/UserSessionState.kt` and `shared/src/main/java/com/furiqd/shared/domain/repository/UserRepository.kt`, following `data-model.md`. Do not implement restoration or profile loading here.
- [x] **T004** Map provider failures to safe outcomes, propagate coroutine cancellation, and test both behaviors in:
    - `core/network/src/main/java/com/furiqd/network/datasource/AuthWithFirebaseAuthenticator.kt`
    - `feature/auth/src/main/java/com/furiqd/auth/presentation/helper/ErrorMapper.kt`
    - `feature/auth/src/main/java/com/furiqd/auth/domain/usecase/AuthUseCases.kt`
    - corresponding tests under `core/network/src/test/` and `feature/auth/src/test/`

## Phase 2: Registration and profile completion (US1)

- [X] **T005 [P]** Test sign-up required-field validation, loading, duplicate taps, and safe provider failures in `feature/auth/src/test/java/com/furiqd/auth/presentation/screens/signup/SignUpViewModelTest.kt`.
- [X] **T006 [P]** Test provider-confirmed registration, profile-save failure, duplicate submissions, uncertain retries, and recovery without a second account in `feature/auth/src/test/java/com/furiqd/auth/domain/usecase/SignUpUseCaseTest.kt`.
- [X] **T007 [P]** Test profile-completion retry for the same identity in `feature/personalisation/src/test/java/com/furiqd/personalisation/domain/SaveUserUseCaseTest.kt` and `feature/personalisation/src/test/java/com/furiqd/personalisation/presentation/screen/onboarding/OnboardingViewModelTest.kt`.
- [x] **T008** Implement approved registration methods, provider-confirmed success, duplicate-submit prevention, and safe reconciliation of uncertain outcomes in the sign-up ViewModel, auth use cases, `AuthWithFirebaseAuthenticator.kt`, and `FirebaseAuthDataSource.kt`.
- [x] **T009** After profile requirements and completion timing are confirmed, route users with an incomplete required profile to Personalisation and support retry without registering again. Update `AppNavigation.kt`, `OnboardingViewModel.kt`, and `SaveUserUseCase.kt`.
- [X] **T010a [P]** Test approved `name` normalization and validation: NFC, trimming, whitespace collapsing, 2–30 Unicode code points, permitted letters/separators, and rejected characters/placement.
- [X] **T010b [P]** Test `nickName` normalization and validation: 2–24 characters, lowercase, allowed Latin letters/digits/`_`/`.`, and rejection of unsupported characters. Test legacy usernames remain accepted for sign-in.

## Phase 3: Login (US2)

- [x] **T011 [P]** Test missing/malformed fields, loading, duplicate submissions, generic credential errors, and recoverable network failure in `feature/auth/src/test/java/com/furiqd/auth/presentation/screens/signin/SignInViewModelTest.kt`.
- [x] **T012 [P]** Test provider-confirmed login, rejection without session creation, and sign-in with legacy-format usernames in `feature/auth/src/test/java/com/furiqd/auth/domain/usecase/SignInUseCaseTest.kt`.
- [x] **T013** Implement login using approved methods, including `nickName` as a login identifier; expose safe outcomes and never authenticate on provider rejection. Update the sign-in ViewModel, auth use cases, and `AuthWithFirebaseAuthenticator.kt`.
- [x] **T014** Route confirmed login using profile availability and the approved neutral destination in `app/src/main/java/com/furiqd/itsalive/AppNavigation.kt`.

## Phase 4: External sign-in (US3, conditional)

Proceed only if the provider is explicitly enabled.

- [x] **T015 [P]** Test Google-to-Firebase credential exchange, Firebase identity/token mapping, and safe cancellation in `GoogleCredentialMappingTest.kt` and `GoogleSignInViewModelTest.kt`.
- [x] **T016** Map Google sign-in results to the Firebase UID and Firebase-issued `AuthResult.token`; do not treat the Google ID token as the Firebase token. Update `AuthRepositoryImpl.kt` and `AuthUseCases.kt`.
- [x] **T017** Treat Credential Manager cancellation as an idle return; map denial/provider errors safely in `AuthWithGoogleHelper.kt` and the sign-in ViewModel.

## Phase 5: Password recovery (US4, conditional)

Proceed only if password authentication and recovery are approved and enabled.

- [x] **T018 [P]** Test neutral recovery confirmation, malformed identifiers, rate limits/provider failures, and no account-existence disclosure in `PasswordRecoveryUseCaseTest.kt`.
- [x] **T019** Implement provider-backed recovery in the existing placeholder route in `AuthGraph.kt`, auth use cases, and `AuthWithFirebaseAuthenticator.kt`. First verify whether an existing screen can host the flow. Do not invent provider policy or recovery copy.

## Phase 6: Session lifecycle (US5)

- [x] **T020 [P]** Test provider session present/absent, profile missing versus load failure, account switching, UID-scoped cache isolation, and stale responses in `UserRepositorySessionTest.kt`. Test offline behavior only against an approved policy.
- [x] **T021** Restore provider session separately from profile loading. Represent missing, incomplete, and failed profiles distinctly; scope updates to the current UID and discard stale responses. Implement in `UserRepositoryImpl.kt` and `MainViewModel.kt`. Apply offline behavior only after approval.
- [x] **T022** Audit `TokenRepository`/`accessToken` consumers. No approved non-Firebase consumer was found; removed app-level Auth token persistence and kept the encrypted local adapter only to clear legacy values on logout.

## Phase 7: Logout (US6)

- [x] **T023 [P]** Test local cleanup after provider sign-out failure, idempotent logout, UID cache isolation, and stale work not restoring the prior identity in `UserRepositoryLogoutTest.kt`.
- [x] **T024 [P]** Test that `LogoutUseCase` propagates `UserRepository.logOut()` results in `LogoutUseCaseTest.kt`.
- [x] **T025** Ensure local sign-out and UID-scoped cleanup complete even if provider sign-out fails; propagate the repository result. Handle app-managed tokens according to T022. Do not add server-side revocation here. Update `UserRepositoryImpl.kt` and `LogoutUseCase.kt`.
- [x] **T026** Prevent late auth/profile responses from restoring a logged-out identity and gate protected navigation on current provider state. Update `UserRepositoryImpl.kt`, `MainViewModel.kt`, and `AppNavigation.kt`.

## Phase 8: Cross-cutting checks

- [x] **T027 [P]** Add app-boundary navigation tests for the approved destination and required profile-completion route in `AuthenticationNavigationTest.kt`. Do not depend on Rooms, Pets, or Pairing internals; do not add an unverified UI-test framework.
- [x] **T028** Audit auth/session UI and diagnostics for raw provider errors, credentials, tokens, or personal data; fix findings and add focused tests.
- [x] **T029** Update `contracts/feature-contracts.md` with observed behavior, evidence paths, and statuses. Do not change `spec.md` or decide open product questions.
- [x] **T030** Run focused builds/tests listed in `quickstart.md`; record only commands and outcomes actually observed. Debug assemble and all listed unit-test tasks passed in this run.
- [ ] **T031** Obtain the human security review required by `constitution.md` CONST-42 and record its disposition here.
- [ ] **T032 [US6]** Implement server-side session/refresh-token revocation only if an approved backend contract requires it. This Android repository has no verified endpoint or backend module; keep blocked until ownership, contract, and target path are supplied.

## Dependencies and blockers

- T002–T004 are foundational. T004 follows T002 because both update `AuthUseCases.kt`.
- T001 gates only tasks that need its unresolved decisions; it does not block policy-independent work.
- T008/T013 require the relevant authentication method to be confirmed. T009/T014/T027 require the corresponding profile timing or destination decision.
- T015–T017 require external sign-in approval. T018–T019 require password recovery approval.
- T020–T021 depend on T003; offline behavior remains blocked until its policy is approved.
- T022 depends on identifying token consumers. T025 must follow that decision. T032 is separately gated by a backend revocation contract.
- T026 is incomplete: repository writes are now guarded against logout races, but `MainViewModel` and protected-route gating are not implemented because approved session/profile routing semantics and the authenticated destination are still unspecified.
- Serialize implementation tasks that edit the same files. Independent tests in separate files may be parallelized.

## Repository constraints

- The destination for T002 does not exist yet; it is the target of moving the existing interface.
- JUnit and `kotlinx-coroutines-test` are available for the relevant modules; no mocking framework was added.
- The app navigation test harness was not verified. Do not introduce a new UI-test framework implicitly.
- The authoritative profile-persistence owner/contract and server-side revocation path are not verified in this Android repository.
- No explicit configuration was found that approves enabled auth methods, verification/recovery, profile-completion timing, offline/session policy, or the authenticated destination. Firebase/Auth SDK wiring alone is not treated as approval.
- Username login needs an approved username-to-provider-identifier lookup contract; Firebase email/password sign-in alone cannot resolve `nickName`. No authoritative profile-validation/atomic-uniqueness contract was found.

## Completion criteria

Complete the applicable approved stories, tests, and security review. Protected routes must require a valid session; logout must remove local access; auth failures must not expose secrets; and authentication must not create or modify pairing, room, invitation, or pet state. Unresolved decisions remain blockers, not inferred requirements.