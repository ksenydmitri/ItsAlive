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

## Relationships

- One provider identity maps to at most one app profile for the same canonical UID.
- A provider identity may be authenticated while its profile is absent or incomplete.
- A profile can exist locally while no provider session is available; this is not an authenticated state.
- The Firebase SDK-managed session is not stored as a Room profile row or recreated from a cached profile.

The exact remote uniqueness and reconciliation constraints require an authoritative backend contract; client code alone cannot guarantee them.

## State transitions

Use behavior equivalent to the specification's states without requiring these exact names in code:

1. `unauthenticated` → `submitting` on a user-initiated sign-in/sign-up attempt.
2. `submitting` → `authenticated` only after provider confirmation, or → `verification_pending` when required by approved provider policy.
3. External sign-in cancellation returns to the authentication screen without a false failure or session.
4. `authenticated` → profile completion when an authenticated identity lacks a completed profile; profile completion is owned by Personalisation.
5. Session restore may resolve to authenticated, unauthenticated/expired, recoverable offline, or recoverable profile-load state. Missing profile must not remain indefinitely indistinguishable as generic loading.
6. Authenticated → `logging_out` → unauthenticated; late refresh/request responses must not restore the logged-out identity.
7. Any failure/cancellation must not create an authenticated transition without provider confirmation.

Offline access and verification-pending semantics must follow approved product/provider policy. These transitions do not define room or post-authentication navigation behavior.

## Validation constraints and approval status

The current specification's detailed `name` and `nickName` rules are Draft. Do not encode them as an approved schema or authoritative policy until product/backend owners reconcile §§4, 9.2, and 12. The current implementation's 2–15 client check is an observed discrepancy, not a target rule.

After approval, profile validation must be shared consistently between client feedback and the authoritative persistence boundary. Username uniqueness must be atomic and case-insensitive if that policy is approved. No specific allowed character set, normalization algorithm, Unicode count unit, or database/API is selected here.
