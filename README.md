# Hey Arlo

A quirky, local-first life coach companion inspired by the warm, oddball energy of 90s animated companions. The name is an original project name, not an official character product.

## Current status

This repository contains the first browser MVP: goals, tasks, check-ins, private journal notes, and a visible privacy control room. Data is stored in the browser's local storage for this prototype and is not sent to a server.

This is **not production-ready secure storage yet**. Before using real sensitive data, replace local storage with an encrypted database backed by platform key stores, add a proper consent/policy engine, and implement authenticated same-network sync.

## Run locally

```bash
npm install
npm run dev
```

## Product guardrails

- No hidden monitoring or silent recording.
- Device capabilities are opt-in and purpose-specific.
- Cloud AI must be disabled by default and disclose what leaves the device.
- Same-network sync must use authenticated end-to-end encryption.
- iOS, Android, Windows, and Linux capabilities must be implemented through each platform's permission model.

## Roadmap

1. Replace prototype storage with encrypted local persistence.
2. Add device pairing and same-network encrypted sync.
3. Add native permission adapters per platform.
4. Add on-device model routing and optional redacted cloud fallback.
5. Add tests, threat modeling, accessibility review, and release signing.
