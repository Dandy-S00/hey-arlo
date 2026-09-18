# Arlo

Arlo is a local-first companion for thoughtful progress: goals, tasks, reflections, private notes, and user-controlled permissions. What I call an AI Habit and Routine Assistant 

## Current phase

The repository includes the Phase 3 encrypted-storage and consent-first AI foundations:

- Web Crypto AES-256-GCM local vault.
- Random salt and initialization vector per encrypted write.
- Versioned encrypted envelope.
- Memory-entry schema with source, confidence, status, and timestamps.
- User-review workflow for personality proposals.
- Source revocation support.
- Explicit policy against silent screen, email, microphone, camera, or disk monitoring.
- Automated vault regression tests, including negative security cases.

## Run locally

```bash
npm install
npm test
npm run dev
```

## Deploy quickly on a Hostinger VPS

The app is packaged as a static production container. It stores user data in the browser's encrypted local vault and does not require a database or server-side secrets.

### One-time VPS setup

Install Docker Engine and the Docker Compose plugin on the VPS, then clone this repository:

```bash
git clone https://github.com/Dandy-S00/hey-arlo.git
cd hey-arlo
chmod +x deploy.sh
./deploy.sh
```

The container listens on port `8080` by default. Set a different host port when needed:

```bash
ARLO_PORT=8090 ./deploy.sh
```

For a domain, point its DNS A record to the VPS and place the container behind the VPS's existing reverse proxy or HTTPS gateway. The container itself remains HTTP-only on the private host port; terminate TLS at the proxy.

### Updating the deployment

```bash
./deploy.sh --pull
```

The script fast-forwards the checkout, rebuilds the image, replaces the container, and prints its health status. The Compose service uses automatic restart, a read-only filesystem, dropped Linux capabilities, and a non-privileged container profile.

## Important security note

Do not treat the app as production-secure until the unlock/migration experience has been integrated and tested end to end. AES-256-GCM protects the local vault, but a lost passphrase cannot be recovered by Arlo.
