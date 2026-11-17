# Quickstart: Authentication Plan Validation

This guide describes validation to run when implementing the plan. It does not claim that the current implementation satisfies the scenarios. See [spec.md](spec.md), [plan.md](plan.md), [data-model.md](data-model.md), and the source-backed [feature contracts](contracts/feature-contracts.md).

## 1. Prerequisites

- Windows, the repository Gradle wrapper, and the Android SDK configured for the repository.
- Local Firebase/Google configuration for manual provider tests. Do not copy credentials, tokens, secret configuration, or sensitive logs into source control or documentation.
- A test Firebase project/accounts or configured Firebase Auth Emulator before running provider integration scenarios. Emulator configuration is not asserted to exist.
- Product/backend decisions in `spec.md` §12 resolved for any acceptance scenario whose expected policy depends on them.

## 2. Build participating modules

From the repository root:

```powershell
.\gradlew.bat :feature:auth:assembleDebug :feature:personalisation:assembleDebug :shared:assembleDebug :core:network:assembleDebug :core:local:assembleDebug :app:assembleDebug
```

**Expected result:** all participating debug variants compile. A successful build is not evidence that authentication acceptance behavior works.

## 3. Run focused unit tests

From the repository root:

```powershell
.\gradlew.bat :feature:auth:testDebugUnitTest :feature:personalisation:testDebugUnitTest :shared:testDebugUnitTest :core:network:testDebugUnitTest :core:local:testDebugUnitTest :app:testDebugUnitTest
```

Run configured instrumented or emulator-backed tests separately. The repository's inspected Auth-related test files are template examples; successful task execution alone is not acceptance coverage.

## 4. Acceptance scenarios

Record each result as **Pass**, **Fail**, **Blocked by product decision**, or **Not implemented**. Preserve the commit/revision and exact command or device setup. Never include credentials or tokens in evidence.

| Scenario | Expected verification |
|---|---|
| Email/password registration and login | Invalid/missing fields are blocked before provider calls; provider rejection stays unauthenticated; repeated taps do not create concurrent requests. Apply only the approved password/identifier policy. |
| Google sign-in token exchange | A Google ID token is exchanged by Firebase; app/session logic uses Firebase identity and obtains Firebase ID tokens from the SDK when needed. Cancellation returns safely without false success. |
| Recovery and verification | Exercise only if product/provider policy enables them; neutral recovery feedback and verification-pending state must not expose account existence or claim full authentication prematurely. |
| Profile completion | Authenticated identity with missing/incomplete profile reaches a recoverable Personalisation state. Profile-save failure can be retried without creating a second provider account. |
| Name and username validation | Test required boundaries and authoritative rejection only after policy approval. Verify case-insensitive uniqueness atomically if approved; otherwise mark blocked rather than choosing a rule. |
| Session restart | Close/relaunch after successful provider authentication; verify SDK session restoration and profile association by canonical UID. A cached profile alone must not produce an authenticated state. |
| Offline restoration | Launch offline with a prior session and cached profile, then with no prior session. Verify only the approved offline experience; never claim fresh provider validation while offline. Include missing local/remote profile recovery. |
| Logout | Verify online, offline, provider failure, repeated logout, account switching, and a request/refresh completing late. Local protected access must end even if remote revocation fails. |
| Cancellation and errors | Cancel provider UI, cancel coroutine work, simulate timeout/network/provider errors, and verify cancellation is not treated as success, errors are safe, and the UI is recoverable. |
| Isolation | Confirm auth operations do not create or mutate rooms, invitations, pairing, or shared-pet state. |

## 5. Evidence and completion

Keep build, unit-test, provider-integration, and device/manual evidence separate. Record the repository revision, Gradle command, emulator/device/provider setup, scenario result, and any blocked product decision. Do not claim a scenario passed because a screen or repository exists.

No builds or tests were run while authoring this planning documentation.
