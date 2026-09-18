# Arlo

Arlo is a local-first companion for thoughtful progress: goals, tasks, reflections, private notes, and user-controlled permissions.

## Current phase

The repository now includes the Phase 3 encrypted-storage and consent-first AI foundations:

- Web Crypto AES-256-GCM vault module.
- Random salt and initialization vector per encrypted write.
- Versioned encrypted envelope.
- Memory-entry schema with source, confidence, status, and timestamps.
- User-review workflow for personality proposals.
- Source revocation support.
- Explicit policy against silent screen, email, microphone, camera, or disk monitoring.

The current UI still uses the earlier prototype storage path until the unlock and migration experience is integrated and tested. Do not treat the app as production-secure yet.

## Important cryptography note

Do not call the current implementation “quantum-proof.” AES-256 is a strong symmetric primitive with a substantial margin against known quantum search attacks, but a complete post-quantum design requires a reviewed hybrid key-establishment protocol and a maintained ML-KEM implementation. See `docs/privacy-and-ai.md`.

## Run locally

```bash
npm install
npm run dev
```

## Next engineering work

1. Integrate the vault into application startup and every state mutation.
2. Add explicit migration from the old plaintext prototype storage.
3. Add lock, passphrase rotation, encrypted export, deletion, timeout, and recovery.
4. Build the memory review screen.
5. Add least-privilege connected email/calendar imports and user-triggered screen sharing only.
6. Add automated tests and cryptographic review.
