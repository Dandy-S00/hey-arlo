# Arlo (Android)

Arlo is a local-first companion for thoughtful progress: goals, tasks, reflections, private notes, adaptive memory, and user-controlled permissions. Built for Android with Kotlin and Jetpack Compose.

## Core Features & Architecture

- **Private Encrypted Local Vault**:
  - Encrypted storage using AES-256-GCM.
  - PBKDF2-SHA-256 key derivation with 600,000 iterations and random salt per vault.
  - Zero cloud reliance for user data; nothing leaves the device without explicit user approval.
  - Unlock and create vault screens with passphrase confirmation and strict 12+ character enforcement.
  - Lock, passphrase change, encrypted JSON backup export, and full vault reset flows.

- **Today (Intentional Progress & Check-in)**:
  - Time-of-day personalized greeting with calendar date.
  - Daily "✦ Check in" reflection dialog with 5 mood states (😵, 😕, 😐, 🙂, ✨) and positive prompts.
  - Hero card: "Progress beats perfect" with quick focus intent.
  - Today's task list with completed counter, quick add, toggle, and deletion.
  - Real-time energy reflection card.

- **Goals (Direction)**:
  - Meaningful goal management ("Keep the why nearby").
  - Title and "Why does it matter?" context with done status toggling.

- **Journal (Private Notebook)**:
  - Local encrypted private notes ("What is on your mind? Nothing leaves this device.").
  - Formatted timestamps and note management.

- **Memory & Adaptive Learning**:
  - Low-friction, consent-first adaptation.
  - Current communication preferences (Tone, Style, Feedback).
  - 7-day adaptive trials workflow with explicit "Keep it" (approves and permanently saves) or "Undo".
  - Structured memory entries with source labels, confidence levels, and revocation controls.

- **Control Room (Privacy & Permissions)**:
  - Per-source toggle switches (Calendar, Location, Notifications, Files, Microphone, Camera, Accessibility, Cloud AI).
  - One-tap "Pause all" emergency privacy switch.
  - Vault management controls and security audit trail (last 50 security actions).

- **Floating Arlo Companion Bubble & API Connectors**:
  - Interactive pulsing Arlo companion bubble with customizable avatars (✦, ☼, ◈, ☁, 🌿, ⭐).
  - Permission Center catalog of 9 external API integrations (Google Calendar, Drive, Notion, Slack, GitHub, Linear, Todoist, Weather, Custom).
  - Consent-first approval flow for external connectors.

## Build & Test

- **Gradle Build**: `gradle assembleDebug` (compiles to debug APK)
- **Unit Tests**: `gradle testDebugUnitTest`
