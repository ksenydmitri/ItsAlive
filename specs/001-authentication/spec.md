# Authentication

**Branch:** `001-authentication`  
**Status:** Draft

## 1. Goal and boundaries

Implement account registration, login, required profile fields, provider-required verification and recovery, session restoration/refresh, and logout.

Authentication establishes the user's identity only. It MUST NOT create, join, modify, or navigate to a room, pairing, invitation, pet, or other post-authentication feature. Those features own their workflows.

## 2. No-invention rule

Before implementation, inspect the repository and its approved product/backend configuration to identify the authentication provider, enabled methods, identifier policy, and provider behavior.

- Implement only methods explicitly enabled in that configuration.
- Do not add a provider, login method, verification flow, recovery flow, password rule, session lifetime, offline policy, or account-linking behavior based on assumptions.
- If a required decision is absent or conflicting, stop the affected work and report it as a blocker. Do not select a default.
- Client validation is for usability only. The backend/provider is authoritative.

## 3. Required profile fields

### `name` — display name

Required. Before validation, normalize Unicode to NFC, trim leading/trailing whitespace, and replace consecutive whitespace with one space. Then require:

- 2–30 Unicode code points;
- Unicode letters; single spaces, hyphens (`-`), and straight apostrophes (`'`) are allowed only between letters;
- no digits, emoji, control characters, typographic apostrophes (`’`), or other symbols.

Measure length in Unicode code points, not UTF-16 code units.

### `nickName` — username

Required and accepted as a login identifier. The user enters it without `@`.

- 2–24 characters, excluding the display prefix `@`.
- Allowed characters: Latin letters `a-z`, `A-Z`, digits `0-9`, underscore (`_`), and dot (`.`). They may occur at any position; consecutive underscores and dots are allowed.
- Reject spaces, hyphens, Cyrillic characters, emoji, and all other characters.
- Normalize and store in lowercase. Compare usernames case-insensitively.
- Enforce uniqueness atomically at the authoritative backend; concurrent creation or changes MUST NOT result in duplicates.
- Add `@` only when displaying the username. Do not store or count the prefix.

Existing usernames that violate these rules MUST remain usable for sign-in and MUST NOT be forcibly changed. Apply the new rules only when creating or changing a username.

Validate both fields in the UI and at the authoritative profile-persistence boundary. Invalid values MUST NOT be persisted. Return structured validation errors.

## 4. Authentication behavior

### Registration

- Validate all required fields before sending a request.
- Create an account and establish authenticated state only after the provider confirms the relevant operation.
- Prevent concurrent duplicate submissions. After timeout or interruption, do not assume the request failed or succeeded; reconcile with the provider before retrying when supported.
- A duplicate registration MUST NOT create a duplicate local identity or session. Show a safe message and the configured next step.
- If profile saving fails after account creation, retain or recover the confirmed identity and allow profile completion to be retried. Do not create another account.
- If provider verification is required, represent the account as pending and do not treat it as fully verified or authenticated until the provider's required conditions are met.
- Never persist a password in ordinary local storage.

### Login

- Accept `nickName` as a login identifier. Accept other identifiers only if explicitly enabled by the existing configuration.
- Validate required fields before sending a request.
- Establish a session only after provider confirmation.
- Use a generic safe response for invalid credentials and unknown identifiers; do not reveal account existence unless explicitly allowed by approved security policy.
- Handle unverified, disabled, locked, suspended, and rate-limited accounts according to provider responses.
- Prevent duplicate concurrent submissions. A timeout or connection loss MUST NOT be treated as confirmed success or failure.
- Fresh login without network connectivity MUST remain unauthenticated.
- Successful login navigates to the configured authenticated entry point, never to a pairing or room destination.

### Verification and password recovery

Implement these flows only when required by the enabled provider configuration.

- Keep verification-pending and verified states distinct.
- Reject invalid, expired, malformed, or already-used verification/recovery codes or links.
- Resend actions, where supported, MUST follow provider cooldowns and rate limits.
- Recovery responses MUST NOT disclose whether an account exists.
- Confirm verification or password-reset success only after provider confirmation.
- Follow the provider's documented policy for session revocation after password reset.

### Session and logout

- Store credentials only in platform-appropriate secure storage. Never log passwords, tokens, verification/recovery codes, or credential payloads.
- Restore and refresh sessions only according to the provider contract.
- Reject expired, revoked, corrupt, incomplete, or wrong-environment credentials. Do not rely solely on the device clock to grant authentication.
- Coordinate concurrent refreshes. Persist rotated credentials safely. A late or stale response MUST NOT overwrite a newer authentication state.
- Without network connectivity, do not claim fresh provider validation. Do not allow protected server operations to appear successful offline.
- Logout MUST immediately remove local authenticated access, including when offline or server-side revocation fails. Revoke remotely where supported.
- Logout MUST be idempotent. In-flight requests or refresh responses MUST NOT restore a logged-out session.
- After logout or account switching, the previous account's credentials and user-scoped data MUST NOT be exposed as belonging to the current user.
- Protected routes MUST reject unauthenticated users, including through back navigation.

## 5. Errors and accessibility

Show loading state during requests and prevent duplicate submission. On recoverable failure, allow retry or the configured recovery action.

Map provider errors to safe, actionable messages. Never display raw provider responses, stack traces, tokens, or secrets. Diagnostics may contain only non-sensitive metadata.

Associate validation errors with their fields or form. Errors MUST be perceivable without color alone and usable with assistive technology and keyboard navigation.

## 6. Required tests

Test enabled flows for:

- field validation, username uniqueness, legacy-username sign-in, and backend rejection of invalid profile data;
- provider-confirmed success versus pending, rejected, failed, or uncertain outcomes;
- duplicate submissions, interruption, timeout, offline behavior, and stale responses;
- session restore, refresh, expiry/revocation, corrupt storage, logout during requests/refresh, and account switching;
- safe errors, secret-free diagnostics, protected-route access, and absence of room/pairing/pet side effects.

Do not mark the feature complete while a required configuration blocker remains unresolved. Authentication tests MUST confirm that authentication creates or modifies no room, pairing, invitation, or pet state.