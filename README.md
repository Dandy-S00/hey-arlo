# Arlo

Arlo is a local-first companion for thoughtful progress: goals, tasks, reflections, private notes, and user-controlled permissions.

## Phase two status

This phase begins the privacy foundation:

- Product name changed from Hey Arlo to **Arlo**.
- Package and document branding updated.
- Local privacy control room retained.
- A visible local audit trail now records important task, journal, goal, check-in, permission, and pause actions.
- Privacy copy now distinguishes prototype storage from production-grade encrypted storage.

The prototype still uses browser local storage. Do not use it for sensitive production data yet. The next security implementation should replace this with encrypted persistence backed by platform key stores.

## Run locally

```bash
npm install
npm run dev
```

## Phase roadmap

1. Validate the product loop with local-only data.
2. Add encrypted persistence and migration tests.
3. Add authenticated same-network pairing and end-to-end encrypted sync.
4. Add native adapters for Android, iOS, Windows, and Linux.
5. Add local model routing and optional redacted cloud fallback.
6. Threat-model, accessibility-test, performance-test, and release-sign every platform build.

## Guardrails

No hidden monitoring, silent recording, keystroke logging, covert camera/microphone use, or access to another person's device without informed consent.
