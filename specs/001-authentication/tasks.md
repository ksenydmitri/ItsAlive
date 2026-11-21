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

## Phase 9: UI Componentization & Figma Design Implementation (Figma URL: https://www.figma.com/make/HwcBwAf6U5Zeyn2h6ldCb9/ItsAlive-Authentication-Screens)

- [x] **T033 [UI]** Implement reusable atomic Form elements in `feature/auth/src/main/java/com/furiqd/auth/presentation/elements/`:
  - `AuthTextField.kt`: Material 3 OutlinedTextField with active Coral border, leading icon, trailing password toggle / clear icon, and error text.
  - `AuthPrimaryButton.kt`: Full-width 56dp Coral button with loading spinner state and disabled state.
  - `PasswordStrengthMeter.kt`: Visual 3-bar indicator for password complexity (Weak / Medium / Strong).
- [x] **T034 [UI]** Refactor `StartScreen.kt` using modular sub-composables in `feature/auth/src/main/java/com/furiqd/auth/presentation/screens/start/`:
  - `StartHeader.kt`: App tagline and pet egg hero graphic.
  - `StartActions.kt`: Google Sign-In button, email sign-in button, and signup text link.
- [x] **T035 [UI]** Refactor `SignInScreen.kt` using modular sub-composables in `feature/auth/src/main/java/com/furiqd/auth/presentation/screens/signin/`:
  - `SignInHeader.kt`: Back button and welcome title.
  - `SignInForm.kt`: Email & password `AuthTextField` components, "Forgot Password?" link, and "Sign In" button.
- [x] **T036 [UI]** Refactor `SignUpScreen.kt` using modular sub-composables in `feature/auth/src/main/java/com/furiqd/auth/presentation/screens/signup/`:
  - `SignUpHeader.kt`: Back button and account creation title.
  - `SignUpForm.kt`: Email input, password input with `PasswordStrengthMeter`, Terms of Service checkbox, and "Create Account" button.
- [x] **T037 [UI]** Implement `ForgotPasswordDialog.kt` in `feature/auth/src/main/java/com/furiqd/auth/presentation/screens/signin/`:
  - Material 3 ModalBottomSheet/Dialog with key icon header, email input, "Send Link" button, and success confirmation state. Connect route in `AuthGraph.kt`.
- [x] **T038 [P]** Create Component UI Unit Tests under `feature/auth/src/test/java/com/furiqd/auth/presentation/elements/`:
  - `AuthTextFieldTest.kt`: Test error text visibility, password masking toggle, and enable/disable states.
  - `PasswordStrengthMeterTest.kt`: Test meter visual state calculation for weak vs strong passwords.
  - `SignInFormStateTest.kt`: Test form button enable logic based on email and password validity.

## Phase 10: UI-Driven Business Logic & ViewModel State Expansion

- [x] **T039 [BL]** Implement Password Complexity Calculation in `feature/auth/src/main/java/com/furiqd/auth/domain/model/PasswordStrength.kt` & `InputValidator.kt`:
  - Calculate `PasswordStrength` (`EMPTY`, `WEAK`, `MEDIUM`, `STRONG`) based on length (>=8 chars), digits, uppercase letters, and special symbols.
  - Update `SignUpState.kt` with `val passwordStrength: PasswordStrength` and update `SignUpViewModel` to calculate strength on password changes.
- [x] **T040 [BL]** Implement Forgot Password ViewModel & Cooldown Timer in `feature/auth/src/main/java/com/furiqd/auth/presentation/screens/signin/ForgotPasswordViewModel.kt`:
  - Create `ForgotPasswordState.kt` (email, emailError, isLoading, isEmailSent, cooldownRemainingSeconds, isSubmitEnabled).
  - Implement 60-second cooldown ticker timer after sending reset email to prevent Firebase API rate-limiting spam.
  - Handle `ForgotPasswordEvent` (EmailChanged, SendResetClicked, DismissDialog) and emit `ForgotPasswordEffect`.
- [x] **T041 [BL]** Enforce Terms & Privacy Policy Acceptance in `SignUpViewModel.kt`:
  - Update `isSignUpEnabled` condition: `isSignUpEnabled = isEmailValid && isPasswordValid && isPrivacyChecked && !isLoading`.
- [x] **T042 [P]** Create Unit Tests for new Business Logic under `feature/auth/src/test/java/com/furiqd/auth/`:
  - `PasswordStrengthCalculatorTest.kt`: Test `PasswordStrength` evaluation for weak, medium, and strong passwords.
  - `ForgotPasswordViewModelTest.kt`: Test 60-second cooldown countdown, success state transition, and error handling.
  - `SignUpViewModelPrivacyCheckTest.kt`: Test that `isSignUpEnabled` remains false when `isPrivacyChecked` is false regardless of valid credentials.

## Phase 11: Backend Network Integration & Resiliency (`:core:network`)

- [x] **T043 [BE]** Refactor `FirebaseAuthDataSource.kt` for Cancellation & Exception Safety:
  - Ensure Firestore `await()` calls propagate `CancellationException` re-throws.
  - Safely catch `FirebaseFirestoreException` and return safe null or typed error results without crashing.
  - Ensure `updateFcmToken` re-throws `CancellationException` before `runCatching`.
- [x] **T044 [BE]** Configure OkHttp Timeouts & Security Interceptor in `BackendlessNetworkModule.kt`:
  - Set explicit 15s connect, read, and write timeouts on OkHttpClient.
  - Ensure HttpLoggingInterceptor redacts sensitive headers in release builds per `constitution.md` CONST-32.
- [x] **T045 [P]** Create Network Data Source Unit Tests under `core/network/src/test/java/com/furiqd/network/`:
  - `FirebaseAuthDataSourceTest.kt`: Test Firestore document deserialization, missing profile handling, and FCM token device registration logic.
  - `BackendlessNetworkModuleTest.kt`: Test Retrofit OkHttpClient timeout configurations and header serialization.

## Dependencies and blockers

- T002–T004 are foundational and completed.
- T008, T013, T014, T016, T017, T019, T021, T025, T026 are completed and verified across auth, personalisation, network, and app navigation modules.
- Phase 10 (T039–T042) business logic tasks should precede or be developed alongside Phase 9 (T033–T038) UI components.
- T032 is separately gated by a server-side backend revocation contract.
- T045 remains open: network unit tests cover profile-existence/decoder helpers, cancellation propagation, FCM request construction, and serialized push headers, but do not exercise Firebase `DocumentSnapshot.toObject` against a Firestore-backed fixture.
- Compose instrumentation tests were compiled successfully but were not run because no Android device/emulator was available.
- Password-strength tier thresholds (weak/medium/strong) are implemented and tested; the source task specified the signals but not the tier mapping.
- UI follows the written task details and existing theme; pixel-level Figma context was not available from the referenced `/make/` URL.

## Repository constraints

- JUnit and `kotlinx-coroutines-test` are available for the relevant modules.
- Form elements in `feature/auth/src/main/java/com/furiqd/auth/presentation/elements/` must be modularized and non-monolithic.
- `AuthGraph.kt` routes `StartDestination`, `SignInDestination`, `SignUpDestination`, and `ForgotPasswordDestination` must map to `AppNavigation.kt`.

## Completion criteria

Complete the applicable approved stories, tests, and security review. Protected routes must require a valid session; logout must remove local access; auth failures must not expose secrets; and authentication must not create or modify pairing, room, invitation, or pet state. Unresolved decisions remain blockers, not inferred requirements.