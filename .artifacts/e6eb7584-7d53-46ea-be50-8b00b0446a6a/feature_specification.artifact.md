# Feature Specification: Life-Sync Pet State & Activity Timeline

## Alignment with README.md

This feature directly implements **Phase 2 (Life-Sync)** and **Phase 3 (Engagement)** of the [ItsAlive README.md](file:///C:/Users/Ksenia/AndroidStudioProjects/ItsAlive/README.md):
- **Lifestyle Integration:** Connects user activity (steps, sleep, challenges) to the virtual pet's health, energy, and mood state.
- **Offline-First & Caching Architecture:** Uses **Room Database** for persistent local storage, **In-Memory Caching (`StateFlow` + `LruCache`)** for instant UI response, and **WorkManager** for reliable background synchronization.
- **Clean Architecture + MVI:** Follows the project's multi-module, reactive MVI architecture.

---

## Business Logic Description

The **Life-Sync Pet & Activity Timeline** feature manages the lifecycle, energy, mood, and historical activity logs of the joint pet.

### 1. Pet State Calculation & Life-Sync Rules
- **Energy Pool (0–100%):** Depletes over time (e.g., 5% per hour of inactivity) and replenishes when partners complete daily challenges or sync steps via Health Connect / local activity.
- **Mood States:**
  - `HAPPY` (Energy > 70%)
  - `NEUTRAL` (Energy 30–70%)
  - `LONELY / SAD` (Energy < 30% or if partner has been inactive for > 24 hours).
- **Experience & Leveling:** Completing daily challenges awards XP to the `JointPetEntity`. Reaching XP thresholds triggers level-ups and visual stage evolutions.

### 2. Caching & Data Flow (The 4 Caching Layers)
1. **In-Memory Cache (RAM):** Repository keeps active pet state and recent activity items in a hot `StateFlow` (`SharingStarted.WhileSubscribed(5000)`) for zero-latency screen transitions.
2. **TTL Cache Invalidation:** When opening the Pet screen, the repository checks `cachedAt`. If older than 5 minutes, a background sync is requested; otherwise, local Room data is rendered immediately.
3. **Persistent Local Cache (Room DB):** All pet stats, streak counters, and past challenge events are stored in SQLite (`joint_pets` and `daily_challenges` tables) for offline access.
4. **Disk / OkHttp Cache:** Photos and asset bundles are cached locally using OkHttp and Coil image loaders.

### 3. Background Synchronization (WorkManager)
- Periodic workers check pet energy decay and queue offline challenge/activity submissions when network connectivity returns.

---

## Exception Criteria & Error Handling

| Scenario | System Behavior | Recovery / Fallback |
| :--- | :--- | :--- |
| **Network Loss during Pet Sync** | API call to update pet state or fetch new challenges throws `IOException`. | Catch exception, log warning, fallback gracefully to latest cached data in **Room Database**, and show a subtle "Offline mode" badge in the UI. |
| **Stale Cache / TTL Exceeded** | `cachedAt` timestamp is older than 5 minutes, but remote server is unreachable. | Serve stale data from **Room DB** to prevent blocking the user, suppress error toast unless user explicitly pulls-to-refresh. |
| **Database Corruption / Read Error** | Room throws `SQLiteException` or serialization failure. | Catch exception, emit default/empty fallback state (`JointPetEntity` default state), and schedule safe database recreation or migration check. |
| **Background Work Failure (WorkManager)** | `UploadChallengeWorker` fails due to server 500 error or dropped connection. | Configure exponential back-off retry policy (`WorkManager` with `BackoffPolicy.EXPONENTIAL`) up to 3 retries. If exhausted, store pending action in local SQLite queue for manual retry. |
| **Process Death / Low Memory** | Android OS kills app process while in background. | In-memory cache is cleared, but persistent **Room DB** and **DataStore** retain state. Upon app restart, UI re-hydrates instantly from Room `Flow`. |
