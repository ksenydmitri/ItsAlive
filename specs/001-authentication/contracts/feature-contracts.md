# Authentication Feature Contracts

**Specification:** `specs/001-authentication/spec.md`  
**Repository revision:** `879dde6`  
**Evidence basis:** Current repository source and the current worktree copy of the specification. The worktree contains pre-existing changes; this document does not modify them. Source inspection only; no tests were run.

## 1. Scope and terminology

This document records the Authentication behavior visible in the repository and compares it with the requirements in the standalone Authentication specification. It does not prescribe new APIs or implement fixes. Pairing, rooms, invitations, and shared-pet behavior are outside scope.

In the observed implementation:

- **Provider identity** is the Firebase Authentication UID.
- **Profile** is `com.furiqd.shared.domain.model.User`, including `name`, `nickName`, and `isOnboardingCompleted`.
- **Display name** in the specification maps to `User.name`; **username** maps to `User.nickName`.
- **Authenticated session state** is exposed as `UserSessionState`; it is driven by Firebase Auth state and the local profile's onboarding flag.
- **Statuses** below distinguish source behavior from requirement compliance: **Observed** means code is present; **Partial** means only part of the requirement is evidenced; **Gap** means observed behavior differs from a requirement; **Not Found** means not found in inspected source; **Needs Verification** means source inspection cannot establish runtime/provider/backend behavior.

The specification is marked **Draft**. Section 9.2 contains specific display-name and username rules, but §4 and §12 still require agreement/confirmation of the policy, including the character set and Unicode length behavior. No approval artifact was found in the inspected repository. Treat §9.2 as draft requirement text, **not an approved product policy**; this document neither adopts a new rule nor changes the specification.

## 2. Observed authentication contracts

| Boundary | Observed responsibility and source |
|---|---|
| `:feature:auth` | `SignInViewModel` and `SignUpViewModel` in `feature\auth\src\main\java\com\furiqd\auth\presentation\screens\signin\SignInViewModel.kt` and `feature\auth\src\main\java\com\furiqd\auth\presentation\screens\signup\SignUpViewModel.kt`; `InputValidator` in `feature\auth\src\main\java\com\furiqd\auth\presentation\helper\InputValidator.kt`; `AuthUseCases` in `feature\auth\src\main\java\com\furiqd\auth\domain\usecase\AuthUseCases.kt`; `AuthRepository` and `TokenRepository` in `feature\auth\src\main\java\com\furiqd\auth\data\repository\`. |
| `:core:network` | `AuthWithFirebaseAuthenticator` in `core\network\src\main\java\com\furiqd\network\datasource\AuthWithFirebaseAuthenticator.kt`; `FirebaseAuthDataSource` in `core\network\src\main\java\com\furiqd\network\datasource\FirebaseAuthDataSource.kt` exposes Firebase UID, reads/writes Firestore profile documents, and signs out. |
| `:shared` | `User`, `UserSessionState`, and `UserRepository` in `shared\src\main\java\com\furiqd\shared\domain\model\User.kt`, `shared\src\main\java\com\furiqd\shared\domain\model\UserSessionState.kt`, and `shared\src\main\java\com\furiqd\shared\domain\repository\UserRepository.kt`; `UserRepositoryImpl` in `shared\src\main\java\com\furiqd\shared\data\repositories\UserRepositoryImpl.kt`; `UserDataMapper` in `shared\src\main\java\com\furiqd\shared\data\mapper\UserMapper.kt`. |
| `:core:local` | `LocalUserEntity`, `UserDao`, and `UserLocalDataSourceImpl` in `core\local\src\main\java\com\furiqd\local\entity\LocalUserEntity.kt`, `core\local\src\main\java\com\furiqd\local\room\UserDao.kt`, and `core\local\src\main\java\com\furiqd\local\datasource\UserLocalDataSourceImpl.kt`; encrypted token DataStore in `core\local\src\main\java\com\furiqd\local\di\StorageModule.kt`, `core\local\src\main\java\com\furiqd\local\storage\TokenSerializer.kt`, and `core\local\src\main\java\com\furiqd\local\storage\CryptoManager.kt`. |
| `:app` | `MainViewModel` in `app\src\main\java\com\furiqd\itsalive\MainViewModel.kt` observes `UserRepository.sessionState`; `MainActivity` in `app\src\main\java\com\furiqd\itsalive\MainActivity.kt` displays loading while it is `Loading`; `AppNavigation` in `app\src\main\java\com\furiqd\itsalive\AppNavigation.kt` routes to Auth, Personalisation, or Home. |

`feature/auth/build.gradle.kts` directly depends on `:shared`, `:core:network`, and `:core:local`. Credential operations in Auth use `AuthWithFirebaseAuthenticator`; profile/session operations use `UserRepository`.

Other sources directly relevant to the findings are `feature\auth\src\main\java\com\furiqd\auth\data\repository\AuthRepository.kt`, `feature\auth\src\main\java\com\furiqd\auth\data\repository\AuthRepositoryImpl.kt`, `feature\auth\src\main\java\com\furiqd\auth\data\repository\TokenRepository.kt`, `feature\auth\src\main\java\com\furiqd\auth\data\repository\TokenRepositoryImpl.kt`, `feature\auth\src\main\java\com\furiqd\auth\presentation\helper\AuthWithGoogleHelper.kt`, `feature\auth\src\main\java\com\furiqd\auth\presentation\helper\ErrorMapper.kt`, `feature\auth\src\main\java\com\furiqd\auth\presentation\navigation\AuthGraph.kt`, `feature\personalisation\src\main\java\com\furiqd\personalisation\presentation\screen\onboarding\OnboardingViewModel.kt`, `feature\personalisation\src\main\java\com\furiqd\personalisation\domain\SaveUserUseCase.kt`, and `feature\home\src\main\java\com\furiqd\home\domain\usecase\LogoutUseCase.kt`.

### Target responsibility model (not current implementation status)

- **Authentication** owns provider identity and authentication-session operations/state: sign-in, sign-up, sign-out, and determining whether a provider session is available.
- **Personalisation** owns profile creation/completion, display-name and username validation, and profile editing.
- **Shared** owns common domain models and repository contracts used across features; provider SDK types and feature UI remain outside those contracts.
- **Core infrastructure** owns provider and local/remote persistence adapters.

This is the target ownership model for interpreting contracts, not a claim that the current code follows it. In the current implementation, Auth use cases create/save `User` profiles, profile validation is in `:feature:personalisation`, and `UserRepository` is in `:shared`.

## 3. Sign-in contracts

### Email and password

`AuthWithFirebaseAuthenticator.signInWithEmailAndPassword` and `signUpWithEmailAndPassword` call Firebase Auth and request a Firebase ID token with `getIdToken(false)`. `AuthRepositoryImpl` adapts those results, and `SignInWithEmailAndPasswordUseCase` / `SignUpWithEmailAndPasswordUseCase` save the returned token through `TokenRepository`.

`InputValidator` checks email format and a client password rule of at least eight characters with uppercase, lowercase, and a digit. The specification leaves the authoritative password policy to approved configuration (§4, §12); whether this client rule agrees with Firebase configuration is **Needs Verification**. The UI disables the email action while loading, but no behavioral test verifies duplicate-submit or provider outcomes.

### Google

`AuthWithGoogleHelper` obtains a **Google ID token** through Credential Manager. `AuthWithFirebaseAuthenticator.signInWithGoogleToken` passes that token to `GoogleAuthProvider.getCredential`, then calls Firebase `signInWithCredential`. On success, it obtains a **Firebase ID token** from the resulting `FirebaseUser.getIdToken(false)` and places it in `AuthResultDto.token`. These tokens have different issuers and purposes: the Google ID token is input to Firebase credential exchange; the Firebase ID token represents the resulting Firebase identity and is the token intended for Firebase/custom-backend authentication.

**Confirmed mapping gap:** `AuthRepositoryImpl.signInWithGoogle` discards `AuthResultDto.token` and constructs the shared `AuthResult` using the input Google ID token (`token = idToken`). The Google use case then persists this value. This is not a Firebase session token.

Credential-manager exceptions become a failure result. Cancellation-specific UX and provider callback validation are **Needs Verification**; no targeted test was found.

### Recovery and verification

`AuthGraph` registers `ForgotPasswordDestination`, but its route body is commented out. A password-recovery flow is **Not Found**. Verification behavior is also **Not Found** in the inspected Auth implementation; whether verification is required depends on provider/product configuration.

## 4. User identity and profile contracts

`FirebaseAuthDataSource.currentUserIdFlow` emits `firebaseAuth.currentUser?.uid` through an `AuthStateListener`; `currentUserId` reads the same Firebase UID. `UserRepositoryImpl` uses this UID to query the user cache and remote profile. This is consistent with the specification's canonical-provider-identity requirement (§9.1); email and display-name fields are not used as the user-table key.

`UserDataMapper` maps between:

- Domain: `shared/domain/model/User.kt`
- Remote: `core/network/dto/UserDto.kt`
- Local: `core/local/entity/LocalUserEntity.kt`

`UserDao` uses `LocalUserEntity.id` as the primary key and `INSERT ... REPLACE` for the same UID. These structures support stable identity and same-key replacement, but do not alone prove correct retry behavior or prevent all inconsistent remote/local states.

Authentication and profile completion are represented separately in data: Firebase UID indicates provider identity; `User.isOnboardingCompleted` indicates profile/onboarding completion. `UserSessionState` collapses those into `Loading`, `NotAuthorized`, `NeedsOnboarding`, or `FullyAuthorized`. No distinct verified/refreshing/offline session state is exposed.

Under the target responsibility model in §2, `User` and `UserRepository` are shared contracts; Authentication should establish/observe provider identity and session, while Personalisation should create and validate profile fields. The current Auth use cases also write profile records, so current ownership is not yet aligned with that target.

## 5. Profile validation contract

### Requirement

Specification §5 (display-name scenarios), §9.2, and FR-001 require client and authoritative persistence validation:

- `name`: required; 2–24 Unicode characters after trimming/normalization; Unicode letters, single internal spaces, hyphens, and apostrophes; no digits, emoji, controls, or other special symbols; NFC normalization; trim edges and collapse repeated whitespace.
- `nickName`: required; 2–24 characters excluding `@`; lowercase Latin letters, digits, and underscores; starts with a Latin letter and ends with a letter or digit; case-insensitive uniqueness; stored lowercase; input omits `@`, which is added only for display.
- Invalid values must not be persisted and failures should be structured for user feedback.

The specification's §4/§12 also leaves policy confirmation open. The policy above is therefore reported as written in the Draft, not as independently approved product policy.

### Observed implementation

`feature\personalisation\src\main\java\com\furiqd\personalisation\presentation\screen\onboarding\OnboardingViewModel.kt` (not `:feature:auth`) validates both fields using trimmed `String.length in 2..15`; the Next action also requires a selected custom avatar. Error flags are calculated separately using the untrimmed value and the same 2–15 range. There is no character-set, NFC, repeated-whitespace normalization, lowercase, or uniqueness check in this ViewModel.

`SaveUserUseCase` in `feature\personalisation\src\main\java\com\furiqd\personalisation\domain\SaveUserUseCase.kt` rejects only a blank `name`. `UserRepositoryImpl.updateUser` maps and persists the profile without applying the §9.2 rules. `FirebaseAuthDataSource.saveUserProfile` in `core\network\src\main\java\com\furiqd\network\datasource\FirebaseAuthDataSource.kt` writes a Firestore document and contains no profile validation. No authoritative server-side validation rule was found in the inspected application sources; backend enforcement remains **Needs Verification**.

**Confirmed gap:** the observed UI's 2–15 limits reject valid 16–24 character values and permit values outside the specified character rules. `String.length` on the JVM counts UTF-16 code units, not Unicode code points or grapheme clusters; the spec's §12 question about Unicode length calculation is unresolved. Blank input does not set the ViewModel's inline name-error flag; the Next button is disabled by the validity calculation, but the specified inline guidance is not established for that case.

## 6. Session restoration contract

`UserRepositoryImpl.sessionState` in `shared\src\main\java\com\furiqd\shared\data\repositories\UserRepositoryImpl.kt` observes `currentUserIdFlow`. With no Firebase UID it emits `NotAuthorized`. With a UID, it calls `syncSessionSilently` to fetch the Firestore profile, then observes the Room user row and maps `isOnboardingCompleted` to `NeedsOnboarding` or `FullyAuthorized`. Sign-in use cases call `UserRepository.syncSession()` but ignore its returned `Result<Unit>`.

Observed constraints:

- The session decision is based on Firebase Auth state, not the mere existence of a cached profile.
- The remote profile fetch occurs before local observation starts. `syncSessionSilently` wraps the fetch in `runCatching`, so fetch failures are suppressed before the local observation proceeds.
- If the remote profile is missing and no local row exists, the mapping yields `Loading`; no recovery/error transition is defined in this path.
- `MainActivity` renders a loading screen for `Loading`; `AppNavigation` has no action for that state.
- Firebase documents that native Android Auth state persists across app restarts, and the Android app can inspect `FirebaseAuth.currentUser` / listen for auth-state changes. The implementation uses `FirebaseAuth.AuthStateListener` and current UID, so session restoration is expected to be Firebase-SDK-managed; it is not implemented by restoring the app's separate DataStore token.
- Offline profile recovery, session expiry, revocation, corruption, and account-switch behavior are **Needs Verification**. The app has no separate cached/offline identity state. Its profile sync fetches remote data before observing Room, so offline startup with a cache is not guaranteed to expose that cache immediately; if no profile exists in either location, state remains `Loading`.

## 7. Token lifecycle and logout contract

### Firebase token and session terminology

- **Google ID token:** obtained from Credential Manager and supplied to Firebase as a Google credential. It is not the Firebase user's session or Firebase ID token.
- **Firebase ID token:** a short-lived Firebase-issued token for authenticating requests to Firebase/custom backends. Firebase documentation describes an approximately one-hour lifetime; `FirebaseUser.getIdToken(forceRefresh)` retrieves one, with `forceRefresh` reserved for explicitly forcing refresh.
- **Firebase authentication session:** the Firebase SDK-managed signed-in state represented in this app by `FirebaseAuth.currentUser`/UID and auth-state events. Firebase's native Android SDK persists this state across app restarts.
- **Firebase refresh token:** a longer-lived credential used by Firebase Auth to obtain new Firebase ID tokens. The Firebase client SDK manages this as part of its session; the app code inspected here neither obtains nor refreshes it directly. Server-side revocation is a separate Admin SDK/backend operation.

For `getIdToken(false)`, the app does not force a refresh: Firebase returns a usable ID token from its managed session and refreshes expired token state as needed. `getIdToken(true)` is for an explicit forced refresh (for example, when a token was invalidated out of band), not routine session restoration.

Firebase documentation references:

- [Android: check the current auth state](https://firebase.google.com/docs/auth/android/start#check_the_current_auth_state)
- [Native authentication-state persistence](https://firebase.google.com/docs/auth/flutter/start#persisting_authentication_state) (the page explicitly states native Android/iOS persistence)
- [Android `FirebaseUser.getIdToken`](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser#getIdToken(boolean))
- [Firebase session and refresh-token lifecycle](https://firebase.google.com/docs/auth/admin/manage-sessions)

### Separate DataStore token

The app stores the result of `getIdToken(false)` after email sign-in/sign-up through `TokenRepositoryImpl`, which injects the `DataStore<String>` provided by `core/local/di/StorageModule.kt`. The Google path currently stores the Google ID token instead, due to the mapping gap in §3. The store uses `TokenSerializer` and `CryptoManager` (Tink AES-256-GCM with an Android Keystore master key). `TokenSerializer.readFrom` catches any exception and returns an empty value, so unreadable/corrupt token data is indistinguishable from an empty token to this contract.

**Necessity assessment:** separate persistence of Firebase ID tokens is not needed to restore Firebase sign-in or use Firebase SDK-managed auth state. The Firebase SDK persists the authentication session and manages its refresh token; the app obtains a Firebase ID token from the current `FirebaseUser` when a token is needed. The inspected `TokenRepository.accessToken` flow has no consumer, and Firebase UID—not the stored token—drives `sessionState`. Consequently, the current extra Firebase ID-token copy is redundant for the observed flows and does not constitute a restorable Firebase session. A distinct non-Firebase consumer requirement could change this assessment, but none was found; do not treat that as justification to keep an unconsumed credential by default.

### Logout

`UserRepositoryImpl.logOut` calls `RemoteAuthDataSource.signOut()` (implemented as local `FirebaseAuth.signOut()`, with no explicit server-side refresh-token revocation call) and then clears local user data. Firebase client sign-out ends the SDK-managed local session; it is not evidence that a server-side refresh token was revoked. Server-side revocation, if required, must be performed by an authorized backend/Admin SDK path.

`UserDao.clearUser()` deletes all user rows. **Confirmed gap:** logout does not call either `TokenRepository.clearToken()` or `TokenLocalDataSource.clearToken()`; no call site for either clear operation was found. If the separately persisted token contains a Firebase ID token, it can remain after Firebase sign-out. Local clearing follows sign-out inside one `runCatching` block, so a thrown sign-out error would also prevent the subsequent cache clear. Offline/failure/repeated logout behavior has no targeted test evidence. `feature/home/domain/usecase/LogoutUseCase.kt` calls `UserRepository.logOut`; it does not add token cleanup.

## 8. Local and remote persistence contracts

`UserRepositoryImpl.saveUser` assigns the active Firebase UID, maps to `UserDto` and `LocalUserEntity`, calls remote save, then saves locally and updates onboarding preferences. `updateUser` follows a similar sequence while preserving the cached email.

`FirebaseAuthDataSource.saveUserProfile` issues `users/{uid}.set(userDto, SetOptions.merge())` but does not await the Firestore task. Therefore, the source establishes invocation order, not confirmed remote completion before local persistence. Local writes use Room `INSERT ... REPLACE`; profile reads during session sync fetch the remote document and then write the local row if a remote profile exists.

The email sign-up and Google sign-in use cases call `userRepository.saveUser(...)` but ignore its returned `Result<Unit>`. Since `saveUser` captures errors in a `Result`, those calls can continue to return success even when profile persistence reports failure. This conflicts with the FR-001 recovery requirement and the registration/profile-completion acceptance scenario. Same-UID Room replacement and Firestore merge are observed, but atomicity, retry reconciliation, and absence of duplicate/inconsistent state are **Needs Verification**.

## 9. Error handling and cancellation contract

`AuthWithFirebaseAuthenticator` catches `Exception` and returns `Result.failure`; Auth use cases also catch `Exception`; `UserRepositoryImpl` uses `runCatching`, including in `syncSessionSilently`. These constructs can convert coroutine cancellation into ordinary failure/suppressed failure rather than reliably propagating cancellation. No cancellation-specific tests were found.

`SignInViewModel` and `SignUpViewModel` expose `error.message` in user-facing effects for provider failures. `ErrorMapper.kt` maps validation errors to strings but does not map Firebase/provider exceptions to safe typed messages. This does not meet the FR-008 requirement to avoid raw provider responses/internals; whether a particular exception message contains sensitive data is provider-dependent and **Needs Verification**.

The ViewModels expose loading state and one-off effects. Buttons are disabled while their relevant loading state is true, but success/failure, error safety, stale responses, and cancellation behavior have no targeted test evidence.

## 10. Security boundaries

**Observed positive boundaries**

- Firebase SDK credential operations are in `core/network/AuthWithFirebaseAuthenticator`.
- Remote DTO, shared domain model, and Room entity are distinct and mapped explicitly.
- The token DataStore serializer encrypts persisted bytes using the configured Tink/Android Keystore mechanism.
- Auth ViewModel logs inspected here contain validation categories or a generic success message, not password or token values.

**Observed concerns / unresolved checks**

- `AuthUseCases.kt` is in the feature `domain/usecase` package but imports `AuthRepository` and `TokenRepository` from the feature `data/repository` package. This reverses the intended domain-to-data dependency direction.
- Token cleanup is not part of the observed logout path.
- Raw provider exception messages reach Presentation.
- The Google result stores the Google input token instead of the Firebase session token.
- No server-side profile validation contract or Firestore rules were inspected; authoritative validation and username uniqueness are unverified.
- No runtime, logging, privacy, or account-switch tests were run. No claim is made that the above security properties are behaviorally verified.

## 11. Acceptance criteria mapping

| Specification requirement | Observed evidence | Status |
|---|---|---|
| US-001 / FR-001: registration; SC-001, SC-003 | Email/password and Google flows exist. Client email/password checks exist. Profile save results are ignored by registration use cases; registration idempotency/recovery is not tested. | **Partial / Gap** |
| US-002 / FR-002: login | Email/password and Google Firebase login exist with loading and navigation effects. Provider policy, safe errors, and concurrency outcomes are not tested. | **Partial / Needs Verification** |
| US-003 / FR-003: Google token exchange; cancellation scenario 2 | Credential Manager obtains a Google ID token; Firebase exchanges it and returns a Firebase ID token. `AuthRepositoryImpl` discards the Firebase token and persists the Google token. Credential errors become generic `Failure`; cancellation is not distinguished from failure in the ViewModel UX. | **Gap** |
| US-004 / FR-004: verification (conditional) | No verification UI or action found. | **Not Found; applicability Needs Verification** |
| US-005 / FR-006: restore session; SC-004; scenarios 6, 10, 16, 17 | Firebase SDK persists native auth state; app listens for UID. Profile fetch precedes Room observation. Offline distinction is absent; with no remote or local profile, state stays `Loading`. Offline cache, missing-profile recovery, expired/revoked session, and no-prior-session outcomes are untested. | **Partial / Gap / Needs Verification** |
| US-006 / FR-007: logout; SC-005; scenarios 2, 3, 5, 6 | `UserRepository.logOut` calls Firebase `signOut`, then clears user/profile data. No explicit server-side refresh-token revocation or DataStore token clear. If sign-out throws, the sequential cache clear is skipped. Offline, provider-failure, in-flight request/refresh, and repeated logout outcomes are untested. | **Gap / Needs Verification** |
| Google/Firebase token lifecycle; FR-003, FR-006 | Firebase ID token is short-lived and the SDK-managed session/refresh token is distinct. App persists a second token string; its access-token flow is unused in inspected code. Google path stores the wrong token type. | **Gap / Redundant persistence in observed flow** |
| Profile-name scenarios in §5 and §9.2; FR-001 | UI uses 2–15 for both fields; no character, normalization, or uniqueness checks at persistence boundary. | **Gap** |
| Username uniqueness; §9.2 Validation and Persistence | No case-insensitive uniqueness lookup or atomic unique-write contract found in inspected app sources. Authoritative backend enforcement is not verifiable from client code. | **Not Found / Needs Verification** |
| US-005 scenario 10: profile missing/unavailable | If neither remote nor local profile exists for a valid Firebase UID, session mapping remains `Loading`; no recoverable profile-load state or duplicate-account handling is evidenced. | **Gap** |
| FR-003 cancellation; FR-008 and §8 cancellation transition | `AuthWithGoogleHelper` catches `GetCredentialException`; ViewModel exposes its message as an error effect. Cancellation-specific return-to-auth behavior and prevention of accidental authenticated transition are untested. | **Gap / Needs Verification** |
| FR-008: safe errors and diagnostics; SC-007 | Raw exception messages are exposed to UI; broad catches can suppress cancellation. No targeted tests. | **Gap** |
| FR-009: authentication isolation; SC-008 | Auth use cases contain no pairing/room/pet operations; no isolation acceptance test found. | **Partial / Needs Verification** |
| SC-006, SC-009: concurrency and correction success | UI disables email buttons while loading; no request-level behavior or validation-correction tests found. | **Needs Verification** |

Inspected test files are `feature\auth\src\test\java\com\furiqd\auth\ExampleUnitTest.kt`, `feature\auth\src\androidTest\java\com\furiqd\auth\ExampleInstrumentedTest.kt`, `feature\personalisation\src\test\java\com\furiqd\personalisation\ExampleUnitTest.kt`, `shared\src\test\java\com\furiqd\shared\ExampleUnitTest.kt`, and the corresponding example tests under `core\local\src\test\java\com\furiqd\local\` and `core\network\src\test\java\com\furiqd\network\`. These are template example unit/instrumented tests; no Auth acceptance tests were found. No tests were run for this documentation task.

## 12. Implementation gaps and verification status

1. **Profile rules — Gap:** UI limits are 2–15 rather than 2–24; required character, Unicode normalization, whitespace, username lowercase, and uniqueness rules are not implemented in the inspected client save boundary. Backend enforcement is **Needs Verification**.
2. **Specification decision — Needs product confirmation:** §9.2 enumerates exact name/username rules, but §4 and §12 still call aspects of the policy undecided. The spec is Draft; do not infer approval or change it as part of this contract document. Product/backend owners must reconcile the conflicting text and approve required timing, allowed characters, normalization, Unicode length counting, and username uniqueness policy.
3. **Profile persistence — Gap:** Firestore profile writes are not awaited, and registration use cases ignore `saveUser` results. Retry consistency and server/local reconciliation lack tests.
4. **Session recovery — Partial:** Firebase UID is the session identity source and profile state comes from Room, but fetch-before-cache, missing-profile `Loading`, offline distinction, expiration, refresh, and stale-response behavior are unverified.
5. **Token lifecycle — Gap:** Google flow persists its input Google token; logout does not clear the stored token; no app-level refresh use was found. Firebase SDK session management makes the extra Firebase ID-token copy unnecessary for the observed Firebase-only session flow. Verify whether an approved non-Firebase backend consumer needs any separate token cache before deciding its lifecycle.
6. **Recovery and verification — Not Found / Needs Verification:** Password recovery is a placeholder; verification depends on product/provider policy and has no inspected implementation.
7. **Error/cancellation handling — Gap:** Raw provider messages reach UI and broad exception wrappers can swallow cancellation.
8. **Architecture and test evidence — Gap:** Feature domain use cases depend on feature data interfaces; inspected test suites contain only examples and do not establish the specified acceptance behavior.

### Remaining product and integration decisions

- The spec's §9.2 name/username policy must be reconciled with §4/§12 and explicitly approved; required timing, permitted format, Unicode count/normalization, and atomic username uniqueness remain open.
- Confirm enabled sign-in methods, password policy, verification applicability, and whether password recovery is supported.
- Confirm session lifetime/revocation/concurrency and the approved offline access policy.
- Confirm whether an approved non-Firebase backend actually needs a separately persisted Firebase ID token; no consumer was found in the inspected source.

No source or specification changes are proposed here. Runtime behavior, Firebase/provider policy, backend validation, Firestore security rules, and product approval remain outside what source inspection alone can establish.
