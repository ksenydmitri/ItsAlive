# Authentication Data Model

**Source:** `specs/001-authentication/spec.md`
**Repository baseline:** `879dde6`

This is a conceptual model for planning and acceptance mapping, not a proposal to add specific classes, database tables, or public APIs. Existing source representations are referenced where useful. Profile-name requirements remain Draft and are not approved by this document.

## Concepts

### Provider identity

| Attribute | Meaning |
|---|---|
| Canonical identifier | Stable identifier issued by the configured authentication provider; current implementation uses Firebase UID. |
| Provider | Configured identity provider/method, if needed to distinguish credentials or linking. Enabled methods remain a product decision. |
| Provider-asserted claims | Claims such as email or verified status, only trusted according to approved provider policy. |

Identity is not keyed by editable email, display name, or username. Passwords and raw provider credential objects are not domain profile data and must not be persisted in the app's ordinary profile store.

### Authentication session

| Attribute | Meaning |
|---|---|
| Provider session availability | Whether the provider SDK reports a signed-in identity/session. Firebase Android SDK state is the current implementation source. |
| Operation state | Sign-in/sign-up submission, verification pending, refresh/restore, logout, or safe failure/cancellation state. |
| Online validation status | Whether provider-side validation has occurred; distinct from locally available/offline presentation. |

The Firebase SDK owns its native session and refresh-token lifecycle. Firebase ID tokens are short-lived request credentials obtained from the SDK when needed; Google ID tokens are external credential-exchange inputs. The app must not treat a cached profile or separately persisted ID token as the authentication session.

### User profile

| Attribute | Meaning |
|---|---|
| Profile identifier | Associated canonical provider identity (currently UID). |
| `name` / display name | User-facing profile name; creation, validation, and editing are owned by Personalisation in the target responsibility model. |
| `nickName` / username | Profile handle in the current shared `User` model; policy and authoritative uniqueness remain subject to product/backend approval. |
| Onboarding/profile-completion status | Indicates whether required profile setup has completed; independent of provider authentication. |
| Other profile data | Existing fields such as avatar are outside the Authentication contract except where Personalisation owns them. |

Remote DTOs, domain models, and local entities remain distinct and are mapped at the data boundaries. Room's user row is associated with the canonical UID. Room persistence alone does not establish a valid provider session.

### Profile cache

A locally cached profile may support the returning-user experience only under the approved offline policy. It must be scoped to its canonical identity, must not be shown as belonging to another account, and must not be interpreted as proof of fresh provider validation.

### Password Complexity & Forgot Password States

| State / Model | Purpose |
|---|---|
| `PasswordStrength` (`EMPTY`, `WEAK`, `MEDIUM`, `STRONG`) | Domain evaluation of password complexity score calculated from length (>=8 chars), digits, uppercase letters, and special symbols. |
| `ForgotPasswordState` | Presentation state for password recovery containing `email`, `emailError`, `isLoading`, `isEmailSent`, `cooldownRemainingSeconds` (60s ticker), and `isSubmitEnabled`. |
| `SignUpState.isPrivacyChecked` | Boolean flag enforcing user acceptance of Terms of Service & Privacy Policy before enabling sign-up. |

## Relationships

- One provider identity maps to at most one app profile for the same canonical UID.
- A provider identity may be authenticated while its profile is absent or incomplete.
- A profile can exist locally while no provider session is available; this is not an authenticated state.
- The Firebase SDK-managed session is not stored as a Room profile row or recreated from a cached profile.

The exact remote uniqueness and reconciliation constraints require an authoritative backend contract; client code alone cannot guarantee them.

## State transitions

The conceptual model distinguishes provider authentication, authentication operations, verification eligibility, and profile availability. These dimensions may change independently and do not require identical state names in implementation.

### Provider authentication

1. `initializing` → `authenticated` when Firebase restores or confirms a signed-in identity.
2. `initializing` → `unauthenticated` when Firebase resolves that no signed-in identity is available.
3. `unauthenticated` → `authenticated` only after provider confirmation.
4. `authenticated(uid)` → `unauthenticated` after successful local Firebase sign-out or when the SDK reports no active identity.
5. Temporary network unavailability must not automatically be interpreted as proof that a previously restored identity is unauthenticated.

### Authentication operations

1. `idle` → `submitting` when a user-initiated sign-in or sign-up begins.
2. `submitting` → `idle` after a successful operation and provider-state update.
3. External provider cancellation returns to an idle state without being reported as an authentication failure.
4. Provider errors produce safe, recoverable operation outcomes.
5. Coroutine cancellation must propagate and must not create a false success or authenticated transition.
6. Overlapping operations and late responses must not overwrite newer authentication state.

### Verification and access eligibility

1. Provider authentication and verification eligibility are independent.
2. A Firebase-authenticated identity may still require email verification under the approved product policy.
3. Verification requirements must not be represented as proof that no provider session exists.
4. Exact access restrictions and verification flows remain subject to product approval.

### Profile availability

1. An authenticated identity may have a `loading`, `missing`, `incomplete`, `complete`, or `load_failed` profile state.
2. A missing profile may lead to profile completion when required by approved product policy.
3. A profile loading failure must not be treated as a missing profile.
4. Profile completion and editing remain owned by Personalisation.
5. Profile data must always be associated with the current canonical UID.

### Session restoration and account switching

1. Session restoration must distinguish provider authentication resolution from profile loading.
2. A cached profile may support permitted offline presentation but cannot establish provider authentication.
3. When the canonical UID changes, profile state must be reloaded or selected for the new identity.
4. Pending asynchronous work for a previous identity must not overwrite the new identity's state.
5. Missing profile data must not remain indefinitely indistinguishable from generic loading.

### Logout

1. `authenticated` → `logging_out` → `unauthenticated` during logout.
2. Repeated logout requests must have defined, safe behavior.
3. Late sign-in, refresh, or profile responses must not restore a logged-out identity.
4. Local profile and credential cleanup must follow the approved data-isolation policy.
5. Local Firebase sign-out and server-side refresh-token revocation are separate operations with distinct semantics.

Offline access, verification requirements, and server-side revocation behavior remain subject to approved product/provider policy. These transitions do not define room or post-authentication navigation behavior.

## Validation constraints and approval status

The current specification's detailed `name` and `nickName` rules are Draft. Do not encode them as an approved schema or authoritative policy until product/backend owners reconcile §§4, 9.2, and 12. The current implementation's 2–15 client check is an observed discrepancy, not a target rule.

After approval, profile validation must be shared consistently between client feedback and the authoritative persistence boundary. Username uniqueness must be atomic and case-insensitive if that policy is approved. No specific allowed character set, normalization algorithm, Unicode count unit, or database/API is selected here.
