# Authentication Feature Contracts

**Specification:** `specs/001-authentication/spec.md`  
**Repository revision:** `879dde6`  
**Evidence basis:** Current worktree source and specification. Focused Gradle unit tests were run for Auth, Personalisation, Shared, Home, and Network; the exact command and outcome are recorded in `tasks.md`.

## 1. Scope and terminology

This document records the Authentication behavior visible in the repository and compares it with the requirements in the standalone Authentication specification. It does not prescribe new APIs or implement fixes. Pairing, rooms, invitations, and shared-pet behavior are outside scope.

In the observed implementation:

- **Provider identity** is the Firebase Authentication UID.
- **Profile** is `com.furiqd.shared.domain.model.User`, including `name`, `nickName`, and `isOnboardingCompleted`.
- **Display name** in the specification maps to `User.name`; **username** maps to `User.nickName`.
- **Authenticated session state** is exposed as `UserSessionState`; it is driven by Firebase Auth state and the local profile's onboarding flag.
- **Statuses** below distinguish source behavior from requirement compliance: **Observed** means code is present; **Partial** means only part of the requirement is evidenced; **Gap** means observed behavior differs from a requirement; **Not Found** means not found in inspected source; **Needs Verification** means source inspection cannot establish runtime/provider/backend behavior.

The specification is marked **Draft**, but its `name` and `nickName` format rules and legacy-username compatibility are approved by `tasks.md`. The authoritative profile-persistence boundary and atomic username uniqueness contract remain unverified.

## 2. Observed authentication contracts

| Boundary | Observed responsibility and source |
|---|---|
| `:feature:auth` | Sign-in/sign-up ViewModels and `AuthUseCases`; `AuthRepository` is defined under `feature\auth\src\main\java\com\furiqd\auth\domain\repository\`. App-level `TokenRepository` was removed; provider SDK state is the session source. |
| `:core:network` | `AuthWithFirebaseAuthenticator` in `core\network\src\main\java\com\furiqd\network\datasource\AuthWithFirebaseAuthenticator.kt`; `FirebaseAuthDataSource` in `core\network\src\main\java\com\furiqd\network\datasource\FirebaseAuthDataSource.kt` exposes Firebase UID, reads/writes Firestore profile documents, and signs out. |
| `:shared` | `User`, `UserSessionState`, and `UserRepository` in `shared\src\main\java\com\furiqd\shared\domain\model\User.kt`, `shared\src\main\java\com\furiqd\shared\domain\model\UserSessionState.kt`, and `shared\src\main\java\com\furiqd\shared\domain\repository\UserRepository.kt`; `UserRepositoryImpl` in `shared\src\main\java\com\furiqd\shared\data\repositories\UserRepositoryImpl.kt`; `UserDataMapper` in `shared\src\main\java\com\furiqd\shared\data\mapper\UserMapper.kt`. |
| `:core:local` | `LocalUserEntity`, `UserDao`, and `UserLocalDataSourceImpl` in `core\local\src\main\java\com\furiqd\local\entity\LocalUserEntity.kt`, `core\local\src\main\java\com\furiqd\local\room\UserDao.kt`, and `core\local\src\main\java\com\furiqd\local\datasource\UserLocalDataSourceImpl.kt`; encrypted token DataStore in `core\local\src\main\java\com\furiqd\local\di\StorageModule.kt`, `core\local\src\main\java\com\furiqd\local\storage\TokenSerializer.kt`, and `core\local\src\main\java\com\furiqd\local\storage\CryptoManager.kt`. |
| `:app` | `MainViewModel` in `app\src\main\java\com\furiqd\itsalive\MainViewModel.kt` observes `UserRepository.sessionState`; `MainActivity` in `app\src\main\java\com\furiqd\itsalive\MainActivity.kt` displays loading while it is `Loading`; `AppNavigation` in `app\src\main\java\com\furiqd\itsalive\AppNavigation.kt` routes to Auth, Personalisation, or Home. |

`feature/auth/build.gradle.kts` directly depends on `:shared`, `:core:network`, and `:core:local`. Credential operations in Auth use `AuthWithFirebaseAuthenticator`; profile/session operations use `UserRepository`.

Other sources directly relevant include `AuthRepositoryImpl.kt`, `AuthWithGoogleHelper.kt`, `ErrorMapper.kt`, `AuthGraph.kt`, Personalisation's `ProfileValidator.kt`, `SaveUserUseCase.kt`, `OnboardingViewModel.kt`, and Home's `LogoutUseCase.kt`.

### Target responsibility model (not current implementation status)

- **Authentication** owns provider identity and authentication-session operations/state: sign-in, sign-up, sign-out, and determining whether a provider session is available.
- **Personalisation** owns profile creation/completion, display-name and username validation, and profile editing.
- **Shared** owns common domain models and repository contracts used across features; provider SDK types and feature UI remain outside those contracts.
- **Core infrastructure** owns provider and local/remote persistence adapters.

This is the target ownership model for interpreting contracts, not a claim that the current code follows it. In the current implementation, Auth use cases create/save `User` profiles, profile validation is in `:feature:personalisation`, and `UserRepository` is in `:shared`.

## 3. Sign-in contracts

### Email and password

`AuthWithFirebaseAuthenticator.signInWithEmailAndPassword` and `signUpWithEmailAndPassword` report success only after Firebase Auth completes with a non-null Firebase user. They no longer fetch or persist an ID token. The Firebase SDK remains responsible for native session restoration and token refresh.

`InputValidator` checks email format and a client password rule of at least eight characters with uppercase, lowercase, and a digit. The specification leaves the authoritative password policy to approved configuration (§4, §12); whether this client rule agrees with Firebase configuration is **Needs Verification**. `SignInViewModel` now rejects invalid fields and duplicate submissions, snapshots submitted values, and maps provider/network failures to a generic error; `SignInViewModelTest` covers those behaviors.

### Google

`AuthWithGoogleHelper` obtains a **Google ID token** through Credential Manager and passes it to Firebase credential exchange. The resulting Firebase user supplies profile claims to `AuthResultDto`; neither the Google credential nor a Firebase ID token is persisted by Auth. Whether Google sign-in is an enabled product method remains **Needs Verification**.

`AuthWithGoogleHelper` treats `NoCredentialException` and exceptions whose message contains a cancellation marker as cancellation; other Credential Manager exceptions become failures. The message-based detection and provider callback behavior are **Needs Verification**; no provider-backed cancellation test was run.

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

- `name`: required; 2–30 Unicode code points after NFC normalization, trimming, and collapsing whitespace; Unicode letters with single internal spaces, hyphens, or straight apostrophes only.
- `nickName`: required; 2–24 ASCII characters excluding `@`; Latin letters, digits, `_`, and `.` in any position; normalized lowercase. Case-insensitive uniqueness must be atomic at the authoritative boundary.
- Existing accounts may retain a legacy `nickName`; authentication must not be blocked solely by its format, and no username change is required to continue using the account. Apply new username rules only on creation or change; a change must pass the approved format and uniqueness validation. An unchanged legacy username remains usable when other profile information is read or updated.
- Invalid values must not be persisted and failures should be structured for user feedback.

The legacy-username compatibility behavior is approved: do not validate or rewrite an unchanged existing handle, and do not reject it during sign-in. The authoritative owner and atomic uniqueness contract remain unverified.

### Observed implementation

Personalisation now validates and normalizes both fields through `ProfileValidator` in the UI and before `SaveUserUseCase` calls the repository. Name length uses Unicode code points; new handles are lowercased and checked against the approved ASCII character set. An unchanged legacy handle is preserved. Focused tests cover normalization, boundaries, rejected characters, persistence rejection, and legacy-handle compatibility.

`UserRepositoryImpl.updateUser` and Firestore rules do not provide an independently verified authoritative validator or atomic unique-handle enforcement. Those backend guarantees remain **Needs Verification**.

## 6. Session restoration contract

`UserRepositoryImpl.sessionState` in `shared\src\main\java\com\furiqd\shared\data\repositories\UserRepositoryImpl.kt` observes `currentUserIdFlow`. With no Firebase UID it emits `NotAuthorized`. With a UID, it calls `syncSessionSilently` to fetch the Firestore profile, then observes the Room user row and maps `isOnboardingCompleted` to `NeedsOnboarding` or `FullyAuthorized`. Sign-in use cases call `UserRepository.syncSession()` but ignore its returned `Result<Unit>`.

Observed constraints:

- The session decision is based on Firebase Auth state, not the mere existence of a cached profile.
- The remote profile fetch occurs before local observation starts. `syncSessionSilently` suppresses non-cancellation fetch failures before local observation proceeds; it propagates coroutine cancellation.
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

Auth no longer has an app-level `TokenRepository`, does not fetch Firebase ID tokens during sign-in/sign-up, and does not persist Google credentials. The legacy encrypted DataStore adapter remains solely so logout can clear values written by older app versions; no active Auth consumer reads or writes those values.

**Necessity assessment:** separate persistence of Firebase ID tokens is not needed to restore Firebase sign-in or use Firebase SDK-managed auth state. The SDK manages session refresh; the app has no identified non-Firebase token consumer. Auth no longer requests or persists an additional ID token. The remaining encrypted DataStore access is used only to clear legacy cached values on logout.

### Logout

`UserRepositoryImpl.logOut` locally gates the current UID immediately, attempts Firebase client sign-out, then clears that UID's cached user row and the legacy token value even if sign-out fails. Cleanup runs non-cancellably; the original failure is returned and coroutine cancellation is rethrown. Local profile persistence is serialized against logout cleanup and rejects an old UID after logout. Focused tests cover provider failure, cancellation, idempotent cleanup, retaining another UID's cached row, denying local session access after sign-out failure, and a delayed profile save completing after logout. Broader account-switch races and server-side refresh-token revocation are not verified or claimed.

## 8. Local and remote persistence contracts

`UserRepositoryImpl.saveUser` assigns the active Firebase UID, maps to `UserDto` and `LocalUserEntity`, calls remote save, then saves locally and updates onboarding preferences. `updateUser` follows a similar sequence while preserving the cached email.

`FirebaseAuthDataSource.saveUserProfile` issues `users/{uid}.set(userDto, SetOptions.merge())` but does not await the Firestore task. Therefore, the source establishes invocation order, not confirmed remote completion before local persistence. Local writes use Room `INSERT ... REPLACE`; profile reads during session sync fetch the remote document and then write the local row if a remote profile exists.

The email sign-up use case now propagates `userRepository.saveUser(...)` failures. The Google sign-in use case still ignores the returned profile-save result when creating a profile; Google method enablement remains unconfirmed. Same-UID Room replacement and Firestore merge are observed, but atomicity, retry reconciliation, and absence of duplicate/inconsistent state are **Needs Verification**.

## 9. Error handling and cancellation contract

`AuthWithFirebaseAuthenticator`, Auth use cases, and the repository's current result wrappers propagate coroutine cancellation. `ErrorMapper.sanitizedMessage()` maps provider failures to a generic authentication error; both sign-in and sign-up UIs use it rather than exposing raw provider messages.

The ViewModels expose loading state and one-off effects. Sign-up and email sign-in validation/loading/duplicate taps and generic provider failures have focused tests; sign-in also maps network failures to the same generic message. Retryable profile completion has focused tests. Google cancellation, sign-in stale responses, and cancellation at provider/UI boundaries remain unverified.

## 10. Security boundaries

**Observed positive boundaries**

- Firebase SDK credential operations are in `core/network/AuthWithFirebaseAuthenticator`.
- Remote DTO, shared domain model, and Room entity are distinct and mapped explicitly.
- The token DataStore serializer encrypts persisted bytes using the configured Tink/Android Keystore mechanism.
- Auth presentation code inspected here sends generic sanitized failures and does not log password or token values.

**Observed concerns / unresolved checks**

- `AuthUseCases.kt` depends on the domain-owned `AuthRepository` contract; the separate app-level `TokenRepository` was removed.
- Legacy token cleanup is part of logout; no active Auth code persists tokens.
- External sign-in configuration and callback/cancellation UX still need provider-policy verification.
- No server-side profile validation contract or Firestore rules were inspected; authoritative validation and username uniqueness are unverified.
- Focused local unit tests now cover profile validation, sign-in failure/cancellation, logout cleanup, and onboarding retry; provider integration, account-switch races, and authoritative backend behavior remain unverified.

## 11. Acceptance criteria mapping

| Specification requirement | Observed evidence | Status |
|---|---|---|
| US-001 / FR-001: registration; SC-001, SC-003 | The current email sign-up path propagates profile-save failure; tests cover provider rejection and profile-save failure. Retry/reconciliation after uncertain provider outcomes and enabled-method approval remain unresolved. | **Partial / Needs Verification** |
| US-002 / FR-002: login | Email/password and Google Firebase login exist with loading and navigation effects. Provider policy, safe errors, and concurrency outcomes are not tested. | **Partial / Needs Verification** |
| US-003 / FR-003: Google token exchange; cancellation scenario 2 | Credential Manager input is exchanged by Firebase; app auth no longer persists either token. Google enablement and cancellation-specific UX remain unverified. | **Partial / Needs Verification** |
| US-004 / FR-004: verification (conditional) | No verification UI or action found. | **Not Found; applicability Needs Verification** |
| US-005 / FR-006: restore session; SC-004; scenarios 6, 10, 16, 17 | Firebase SDK persists native auth state; app listens for UID. Profile fetch precedes Room observation. Offline distinction is absent; with no remote or local profile, state stays `Loading`. Offline cache, missing-profile recovery, expired/revoked session, and no-prior-session outcomes are untested. | **Partial / Gap / Needs Verification** |
| US-006 / FR-007: logout; SC-005; scenarios 2, 3, 5, 6 | Logout gates the local UID, clears profile/token caches even on provider failure, and propagates the result. Server revocation and full account-switch/stale-response coverage remain unverified. | **Partial / Needs Verification** |
| Google/Firebase token lifecycle; FR-003, FR-006 | Firebase SDK owns the session; Auth no longer keeps a second app-level ID-token copy. Legacy encrypted token data is cleared on logout. | **Implemented for observed Firebase-only flows** |
| Profile-name scenarios in §5 and §9.2; FR-001 | Personalisation validates the approved name/username formats and normalizes new values before repository persistence. Server-side validation and atomic uniqueness remain unverified. | **Partial / Needs Verification** |
| Username uniqueness; §9.2 Validation and Persistence | No case-insensitive uniqueness lookup or atomic unique-write contract found in inspected app sources. Authoritative backend enforcement is not verifiable from client code. | **Not Found / Needs Verification** |
| US-005 scenario 10: profile missing/unavailable | If neither remote nor local profile exists for a valid Firebase UID, session mapping remains `Loading`; no recoverable profile-load state or duplicate-account handling is evidenced. | **Gap** |
| FR-003 cancellation; FR-008 and §8 cancellation transition | Credential Manager cancellation is handled in the helper/ViewModel path, but no provider-backed cancellation test was run. | **Needs Verification** |
| FR-008: safe errors and diagnostics; SC-007 | Sign-in/sign-up use safe generic provider errors, with focused mapper and ViewModel tests. Provider-side diagnostic behavior remains unverified. | **Partial** |
| FR-009: authentication isolation; SC-008 | Auth use cases contain no pairing/room/pet operations; no isolation acceptance test found. | **Partial / Needs Verification** |
| SC-006, SC-009: concurrency and correction success | UI disables email buttons while loading; no request-level behavior or validation-correction tests found. | **Needs Verification** |

Focused test files include Auth `SignInUseCaseTest.kt`, `SignInViewModelTest.kt`, `SignUpViewModelTest.kt`, and `ErrorMapperTest.kt`; Core Network `AuthWithFirebaseAuthenticatorTest.kt`; Personalisation `ProfileValidatorTest.kt` and `OnboardingViewModelTest.kt`; Shared `UserRepositoryLogoutTest.kt`; and Home `LogoutUseCaseTest.kt`. The Gradle unit-test tasks passed, but no provider-backed integration test was added.

## 12. Implementation gaps and verification status

1. **Profile rules — Partial:** Approved client validation and normalization are implemented and covered by tests. Backend validation and atomic uniqueness remain **Needs Verification**.
2. **Specification decision — Approved:** Name/username formats and unchanged legacy-handle compatibility are approved. Do not validate or rewrite an unchanged legacy handle.
3. **Profile persistence — Gap:** Firestore profile writes are not awaited. Email sign-up propagates local/remote profile-save failures, but retry consistency and server/local reconciliation still lack backend integration tests.
4. **Session recovery — Partial:** Firebase UID is the session identity source and profile state comes from Room, but fetch-before-cache, missing-profile `Loading`, offline distinction, expiration, refresh, and stale-response behavior are unverified.
5. **Token lifecycle — Implemented for observed flows:** app-level Auth token persistence was removed after no consumer was found; legacy encrypted values are cleared at logout. Firebase SDK session management remains authoritative.
6. **Recovery and verification — Not Found / Needs Verification:** Password recovery is a placeholder; verification depends on product/provider policy and has no inspected implementation.
7. **Error/cancellation handling — Partial:** User-facing Auth errors are sanitized and current Auth/repository wrappers preserve cancellation. Credential Manager cancellation behavior and end-to-end error cases remain unverified.
8. **Architecture and test evidence — Partial:** Core Auth repository ownership is corrected and focused unit tests pass; registration/login concurrency, backend integration, account switching, and protected-route acceptance remain untested.

### Remaining product and integration decisions

- Legacy-username backward compatibility and the name/username formats are approved in spec §9.2. Required profile-completion timing and authoritative atomic-uniqueness contract remain open.
- Confirm enabled sign-in methods, password policy, verification applicability, and whether password recovery is supported.
- Confirm session lifetime/revocation/concurrency and the approved offline access policy.
- No approved non-Firebase token consumer was found; if one is identified, its token contract and secure lifecycle must be specified.

Runtime behavior, Firebase/provider policy, backend validation, Firestore security rules, and product approval remain outside what source inspection alone can establish.
