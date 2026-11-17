# Authentication Research

**Source specification:** `specs/001-authentication/spec.md` (Draft)
**Repository baseline:** `879dde6`
**Evidence:** Current source inspection summarized in [contracts/feature-contracts.md](contracts/feature-contracts.md); no tests were run for this planning work.

This research resolves technical planning questions where repository or provider documentation supplies evidence. It does not approve any product rule in the Draft specification.

## Decisions

### 1. Keep provider identity/session distinct from profile completion

**Decision:** Model provider authentication and profile completion as separate concerns. Firebase UID/auth state establishes provider identity; a profile's onboarding-completion flag is profile state, not proof of a valid provider session.

**Rationale:** `FirebaseAuthDataSource` exposes Firebase UID/auth-state changes, while `UserRepositoryImpl` combines that identity with `User.isOnboardingCompleted` to produce the app's current session state. The current composition can remain, but the design and tests must preserve the distinction and define recovery for a valid identity with a missing profile.

**Alternatives considered:** Treating any cached profile as signed-in is rejected because a local profile can outlive or disagree with provider session state. Treating every authenticated provider identity as profile-complete is also rejected.

**Evidence:** `core/network/src/main/java/com/furiqd/network/datasource/FirebaseAuthDataSource.kt`; `shared/src/main/java/com/furiqd/shared/data/repositories/UserRepositoryImpl.kt`; `shared/src/main/java/com/furiqd/shared/domain/model/User.kt`; current observed behavior and gaps in contract sections 4 and 6.

### 2. Use Firebase SDK-managed native session state

**Decision:** Use Firebase Auth's Android SDK-managed signed-in state for restoration and refresh; obtain a Firebase ID token on demand when a request requires it. Do not treat the app's separate DataStore token as the session source of truth.

**Rationale:** Firebase documents native Android auth-state persistence and exposes the current user/auth-state listener. `FirebaseUser.getIdToken(false)` retrieves a usable Firebase ID token without requesting a forced refresh; token refresh/session management is SDK-owned. A separately stored ID token is not needed to restore the Firebase session.

**Alternatives considered:** Manually restoring Firebase auth from a separately persisted ID token is rejected. Keeping a distinct app token cache is deferred unless an approved non-Firebase consumer is found and its secure lifecycle is specified.

**Evidence:** `core/network/src/main/java/com/furiqd/network/datasource/FirebaseAuthDataSource.kt`; `core/local/src/main/java/com/furiqd/local/di/StorageModule.kt`; `core/local/src/main/java/com/furiqd/local/storage/TokenSerializer.kt`; `feature/auth/src/main/java/com/furiqd/auth/data/repository/TokenRepositoryImpl.kt`; Firebase [Android auth-state guidance](https://firebase.google.com/docs/auth/android/start#check_the_current_auth_state), [FirebaseUser.getIdToken](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser#getIdToken(boolean)), and [session management](https://firebase.google.com/docs/auth/admin/manage-sessions). The existing analysis found the stored access-token flow has no consumer and Firebase UID drives `sessionState`.

### 3. Preserve Google and Firebase token roles

**Decision:** A Google ID token is a credential-exchange input. A Firebase ID token is issued after Firebase validates the Google credential. They are not interchangeable with Firebase's SDK-managed authentication session or its refresh token.

**Rationale:** `AuthWithGoogleHelper` obtains the Google token; `AuthWithFirebaseAuthenticator` passes it to `GoogleAuthProvider` and receives a Firebase token. The current `AuthRepositoryImpl` maps the Google input token into the app result instead of the Firebase result, which is a confirmed contract gap.

**Alternatives considered:** Persisting the Google ID token as if it were the app's Firebase access token is rejected. Persisting a Firebase refresh token in app DataStore is not planned; the Firebase SDK owns that lifecycle.

**Evidence:** `feature/auth/src/main/java/com/furiqd/auth/presentation/helper/AuthWithGoogleHelper.kt`; `core/network/src/main/java/com/furiqd/network/datasource/AuthWithFirebaseAuthenticator.kt`; `feature/auth/src/main/java/com/furiqd/auth/data/repository/AuthRepositoryImpl.kt`; Firebase's [Google sign-in guidance](https://firebase.google.com/docs/auth/android/google-signin).

### 4. Keep module ownership aligned with the agreed target model

**Decision:** Auth owns sign-in/sign-up/session operations; Personalisation owns profile creation, validation, and editing; Shared owns common domain models and repository contracts; core modules own remote/provider and local-storage adapters; App composes navigation.

**Rationale:** This respects current module boundaries while avoiding provider SDK types in domain/presentation contracts and avoiding feature-to-feature cycles. The contract document identifies current deviations, including Auth use cases writing profiles and importing repository interfaces from the feature data package.

**Alternatives considered:** Moving feature UI/orchestration into App or moving all auth/profile behavior into Shared is rejected. Direct feature-to-feature dependencies are avoided; App can coordinate the transition from authenticated identity to profile completion.

**Evidence:** `feature/auth/build.gradle.kts`; `feature/auth/src/main/java/com/furiqd/auth/domain/usecase/AuthUseCases.kt`; `shared/src/main/java/com/furiqd/shared/domain/repository/UserRepository.kt`; `feature/personalisation/src/main/java/com/furiqd/personalisation/domain/SaveUserUseCase.kt`; `app/src/main/java/com/furiqd/itsalive/AppNavigation.kt`; target responsibility model in contract section 2.

### 5. Treat profile policy as an implementation gate

**Decision:** Do not implement or claim approval of the name/username rules until the Draft's Section 4/12 conflicts and product decisions are resolved. Once approved, client feedback and authoritative persistence must enforce the same rule; username uniqueness must be atomic at the authoritative boundary.

**Rationale:** The spec contains detailed 2–24 name/username rules but also identifies the character, Unicode counting, ownership/timing, and related policy as requiring confirmation. Current Personalisation UI uses a different 2–15 check, and no server-side validation/uniqueness contract was found in the inspected sources.

**Alternatives considered:** Adopting either the current 2–15 behavior or the Draft's detailed policy as approved without product confirmation is rejected.

**Evidence:** `feature/personalisation/src/main/java/com/furiqd/personalisation/presentation/screen/onboarding/OnboardingViewModel.kt`; `feature/personalisation/src/main/java/com/furiqd/personalisation/domain/SaveUserUseCase.kt`; `core/network/src/main/java/com/furiqd/network/datasource/FirebaseAuthDataSource.kt`; specification §§4, 9.2, and 12; contract section 5.

### 6. Plan explicit recovery, logout, and failure evidence

**Decision:** Require distinct outcomes for missing/stale profile, offline launch, session invalidation, logout failure, cancellation, and late responses. Local logout must make protected access unavailable even if provider-side revocation cannot complete.

**Rationale:** Current session sync fetches remote profile before observing the local cache; a missing remote and local profile can remain `Loading`. Current broad `runCatching`/`Exception` handling can swallow cancellation. Logout calls Firebase sign-out before clearing user rows, so a thrown sign-out can skip later cleanup; stored token cleanup is not invoked.

**Alternatives considered:** Treating a network failure as an empty profile, silently suppressing cancellation, or leaving local authenticated access dependent on server revocation is rejected.

**Evidence:** `shared/src/main/java/com/furiqd/shared/data/repositories/UserRepositoryImpl.kt`; `core/network/src/main/java/com/furiqd/network/datasource/AuthWithFirebaseAuthenticator.kt`; `feature/auth/src/main/java/com/furiqd/auth/domain/usecase/AuthUseCases.kt`; `shared/src/main/java/com/furiqd/shared/data/repositories/UserRepositoryImpl.kt` logout path; spec §§5–8; contract sections 6–9.

## Unresolved product decisions

The following remain exactly as gates, not research conclusions:

- Enabled authentication methods and accepted identifiers.
- Password, verification, recovery, account-linking, rate-limit, and approved user-facing copy policies.
- Whether/when profile completion is required and the approved display-name/username format, Unicode behavior, and uniqueness policy.
- Session lifetime, revocation, concurrent sessions, offline access, and account-switch behavior.
- The neutral authenticated destination and any non-Firebase ID-token consumer.
- Quantitative latency/measurement targets.

The plan can proceed as a design artifact, but implementation and acceptance claims that depend on these choices must wait for product/backend decisions.
