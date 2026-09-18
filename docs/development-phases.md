# Arlo development phases and verification plan

## Phase 0 — Product definition and boundaries

### Goal
Turn a broad idea into a safe product contract. Decide who Arlo serves, which behaviors are helpful, which data sources are optional, and what Arlo must never do.

### Why this comes first
Device-aware coaching touches sensitive data. Building integrations before defining consent, retention, and safety rules creates expensive rework and privacy risk.

### Typical work

- Define the primary user and first five use cases.
- Separate user-authored data, raw observations, and derived insights.
- Write permission-by-purpose requirements.
- Define the global pause, deletion, export, and correction flows.
- Define platform capability differences.
- Write abuse cases: stalking, coercive control, account takeover, accidental disclosure, and model overreach.

### Verification

- A reviewer can explain every stored field and why it exists.
- Every proposed capability has an explicit opt-in and revocation path.
- A privacy review confirms that the minimum data needed is collected.
- A written “not supported” list exists for covert surveillance behaviors.

## Phase 1 — Experience prototype

### Goal
Validate that the coaching loop is useful before investing in native integrations or AI infrastructure.

### Why
A polished interface cannot rescue a confusing or unwanted coaching experience. Local mock data makes it fast and safe to change.

### Typical work

- Build goals, tasks, check-ins, notes, and privacy controls.
- Use friendly empty states and clear feedback.
- Avoid claims that the app can see or understand more than it actually can.
- Test keyboard navigation, small screens, reduced motion, and readable contrast.

### Verification

- Add, edit, complete, and delete a task.
- Add and complete a goal.
- Create and reload a journal note.
- Toggle each privacy control and confirm the state survives reload.
- Use Pause all and verify every permission turns off.
- Check the browser console for errors.
- Run `npm run build` and confirm a clean production build.

## Phase 2 — Privacy foundation

### Goal
Make local data durable, inspectable, auditable, and ready to migrate to encrypted storage.

### Why
Local-first is not automatically private. Browser storage can be read by code running in the same origin, and plain JSON storage is not sufficient for sensitive production data.

### Typical work

- Create a versioned data schema.
- Add migrations between schema versions.
- Replace plain storage with an encrypted database.
- Protect encryption keys with the platform key store or a user-controlled passphrase.
- Add export and deletion flows.
- Add a bounded, tamper-evident local audit log.
- Define retention for raw observations and derived insights.
- Add redaction before any optional cloud request.

### Verification

- Start from each previous schema version and run migrations.
- Confirm a wrong passphrase cannot unlock data.
- Confirm decrypted data is not written to logs, URLs, analytics, or crash reports.
- Inspect browser and application storage for plaintext journal content.
- Export data, delete it, and verify it cannot be recovered through the app.
- Verify the audit log contains no secret content.
- Test interrupted writes and power-loss recovery.
- Run dependency and static security scans.

## Phase 3 — Device pairing and same-network sync

### Goal
Synchronize only the user's chosen records between devices on the same network without creating a readable central copy.

### Why
Sync is a security boundary. A Wi-Fi password alone does not authenticate a peer, and copying an entire database file creates corruption and conflict problems.

### Typical work

- Generate a long-term identity key per device.
- Pair with QR or numeric verification and a safety phrase.
- Discover peers using local network discovery.
- Establish mutually authenticated encrypted channels.
- Sync structured records with IDs, versions, tombstones, and acknowledgements.
- Preserve both versions of high-value conflicting notes.
- Add device list, revoke, re-pair, and wipe controls.

### Verification

- Pair two fresh devices and compare the displayed safety phrase.
- Reject an unpaired device.
- Verify captured network traffic contains ciphertext, not note or task content.
- Disconnect the network during a sync and confirm safe retry behavior.
- Edit the same note on two devices and verify the conflict flow.
- Delete a record on one device and verify deletion propagates.
- Revoke a device and confirm it cannot sync again.
- Test replayed, reordered, truncated, and modified messages.

## Phase 4 — Native platform adapters

### Goal
Use each operating system's approved permissions without pretending all platforms expose the same information.

### Why
Android, iOS, Windows, and Linux have different background execution and privacy rules. A shared user experience needs platform-specific implementation and honest capability labels.

### Typical work

- Implement one adapter at a time.
- Request permission only at the moment it is needed.
- Keep raw data collection separate from coaching logic.
- Prefer summaries over raw histories.
- Make background work visible and stoppable.
- Handle denied, restricted, expired, and revoked permissions.

### Verification

- Test first install, upgrade, permission denial, revocation, and reinstall.
- Test offline behavior and battery-saving modes.
- Verify no background component runs after global pause.
- Confirm precise location can be reduced to approximate location.
- Test platform privacy dashboards and system indicators.
- Review app-store policy requirements before release.

## Phase 5 — Local and hybrid AI

### Goal
Provide useful coaching offline while making every remote inference an explicit, minimized choice.

### Why
Local models improve privacy and reliability. Cloud models can improve quality but create disclosure, retention, cost, and availability risks.

### Typical work

- Define structured inputs and outputs for coaching.
- Route simple tasks to a local model.
- Add confidence labels and source explanations.
- Add a redaction gateway before cloud calls.
- Show what categories leave the device.
- Add model timeouts, rate limits, offline fallback, and cost limits.
- Prevent medical, legal, or mental-health diagnoses from being presented as facts.

### Verification

- Run the same prompts offline and online and compare safety behavior.
- Confirm cloud mode is disabled by default.
- Use synthetic test data to verify names, addresses, precise locations, and contact details are removed.
- Test prompt injection in notes and external content.
- Check that model output cannot enable a disabled permission.
- Measure latency, memory use, battery impact, and failure recovery.
- Maintain a red-team test set for coercive, manipulative, and crisis content.

## Phase 6 — Reliability, accessibility, and release

### Goal
Make the product dependable and safe for real users across supported platforms.

### Why
Sensitive apps need more than feature tests. Data loss, confusing consent, inaccessible controls, and silent failures can directly harm users.

### Typical work

- Unit, integration, end-to-end, property, and security tests.
- Crash reporting that excludes private content.
- Accessibility review with keyboard, screen reader, contrast, text scaling, and reduced motion.
- Backup and recovery drills.
- Signed builds and reproducible release records.
- Threat-model updates after each new integration.
- Staged rollout and rollback plan.
- Clear privacy policy, data inventory, and deletion instructions.

### Verification

- Restore a user profile from an encrypted backup.
- Upgrade across several app versions.
- Run the complete test suite on supported operating systems.
- Validate no secrets appear in build artifacts or logs.
- Confirm release signatures and checksums.
- Test rollback after a failed update.
- Have an independent reviewer repeat the critical consent and deletion tests.

## Minimum test pyramid

- **Unit tests:** encryption helpers, migrations, redaction, conflict resolution, permission policy.
- **Integration tests:** storage, sync protocol, native adapters, model routing.
- **End-to-end tests:** onboarding, pairing, coaching loop, pause, export, deletion.
- **Security tests:** authorization, replay resistance, malformed messages, secret leakage, dependency vulnerabilities.
- **Usability tests:** can a new user understand what Arlo can see, what it stores, and how to stop it?
- **Performance tests:** startup time, sync time, battery, memory, offline behavior, and model latency.

## Definition of done for every phase

A phase is not complete when the code merely runs. It is complete when:

1. The intended behavior works on the supported platforms.
2. Failure and permission-denied paths are tested.
3. Data collected and stored is documented.
4. Security and privacy checks pass.
5. The user can understand, pause, correct, export, and delete the relevant data.
6. The next phase has a stable interface to build on.
