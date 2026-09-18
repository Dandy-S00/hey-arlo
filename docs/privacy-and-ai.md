# Arlo encrypted persistence and consent-first AI

## What was added

Arlo now contains a Web Crypto vault foundation in `src/security/vault.js` and a memory/personality policy foundation in `src/security/memory-policy.js`.

The vault uses:

- AES-256-GCM for authenticated encryption.
- A random salt per vault.
- A random initialization vector per write.
- PBKDF2-SHA-256 with 600,000 iterations to derive a key from a user passphrase.
- A versioned envelope so the storage format can be migrated.

This is a browser-native encrypted-storage foundation, not a completed production vault. The current UI still uses its earlier local-storage path until the unlock, migration, recovery, and deletion flows are integrated and tested.

## Quantum-resistant language

“Quantum-proof” should not be promised for this browser prototype. AES-256 has a large security margin against known quantum search attacks, but passphrase derivation, device compromise, implementation bugs, and future cryptographic research still matter. Web Crypto does not directly provide a post-quantum key-establishment protocol.

The correct production direction is a reviewed hybrid design:

1. Use a modern post-quantum KEM, such as a standardized ML-KEM implementation.
2. Combine its shared secret with a classical authenticated key exchange during a transition period.
3. Use a vetted, maintained library rather than implementing cryptography in application code.
4. Pin versions, monitor advisories, and obtain an independent cryptographic review.
5. Make the storage envelope versioned so algorithms can be rotated.

## Memory file design

Arlo's memory should be a user-controlled structured record, not an invisible profile. Each entry should include:

- A short summary, never an unnecessary raw transcript.
- The source category.
- A human-readable source label.
- Confidence or confirmation state.
- Created, updated, and revoked timestamps.
- An active, revoked, or pending-review status.

The AI may suggest a memory entry or personality adjustment, but it must not silently apply it. The user should be able to edit, approve, reject, export, and delete every entry.

## Screen, email, and calendar data

Arlo should not silently watch the screen, read mail, record audio, or inspect another person's device. The safe design is:

- Screen context: only a visible, user-triggered share action; show exactly what is shared.
- Email: an explicit connected mailbox integration with selected folders, time range, and fields; never an always-on mailbox mirror.
- Calendar: selected calendars and event fields only.
- Files: individual file selection, not unrestricted disk crawling.
- Microphone and camera: explicit system permissions and visible indicators.
- Cloud AI: off by default, with a preview of redacted data before sending.

The user must be able to pause all sources immediately. Revoking a source should stop future collection and mark previously derived memories for review or deletion.

## Personality adaptation

Personality should mean communication preferences, not diagnosis. Good examples include:

- prefers concise or detailed explanations;
- prefers direct or gentle feedback;
- likes reminders in the morning;
- prefers questions before suggestions.

Do not infer or store sensitive traits such as medical conditions, political beliefs, sexual orientation, or psychological diagnoses merely because a model guesses them from text. Treat those categories as prohibited unless the user intentionally supplies and explicitly asks Arlo to retain them.

## Integration checklist

1. Replace the current synchronous app load with an unlock/create-vault screen.
2. Migrate existing unencrypted prototype data only after explicit user confirmation.
3. Write encrypted state on every mutation, with crash-safe temporary records.
4. Add passphrase change, export, delete, lock, timeout, and recovery flows.
5. Add memory review UI with approve, reject, edit, revoke-source, and delete actions.
6. Add connected-app consent screens with least-privilege scopes.
7. Add tests for wrong passphrases, tampered ciphertext, migration, interrupted writes, deletion, and data redaction.
8. Add a cryptographic review before calling the vault production-ready.
