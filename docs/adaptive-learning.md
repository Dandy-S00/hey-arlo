# Arlo adaptive learning: easy by default, visible by design

The target user should not need to understand encryption, data schemas, model settings, or permission matrices. Arlo should do the technical work while making important behavior understandable.

## Product principle

**Automatic does not mean invisible.** Arlo can use safe defaults, learn locally, and try low-risk improvements without forcing configuration screens. It must still show the user what changed, provide an easy “keep it” or “undo” choice, and never create a permanent sensitive profile silently.

## Recommended experience

### 1. One plain-language setup choice

Ask one question during onboarding:

> “Should Arlo learn from what you share with it to improve how it helps you?”

Offer simple choices:

- Yes, keep it private on this device.
- Ask me before using new types of information.
- Not now.

Do not expose cryptographic terminology in onboarding. Store the technical policy behind the scenes.

### 2. Progressive source activation

Do not ask the user to configure every source at startup. Start with information the user directly gives Arlo. When a new source would materially improve assistance, ask at the moment of need in plain language:

> “I can use selected calendar events to make this reminder more useful. Use this calendar?”

The user should never have to locate a settings page to understand or stop a source.

### 3. Local learning loop

The model should process the minimum useful representation on the device:

1. Observe an allowed local signal.
2. Convert it into a short, non-sensitive candidate summary.
3. Discard raw material according to the retention policy unless the user explicitly saves it.
4. Test a low-risk communication adjustment for a limited period.
5. Ask whether the adjustment feels better.
6. Keep, undo, or ignore the change based on the response.

### 4. Low-risk automatic trials

Good automatic trials include:

- shorter responses;
- more direct next steps;
- gentler wording;
- fewer reminders;
- a morning rather than evening check-in;
- asking a question before offering advice.

The trial should be temporary, visible, reversible, and limited to communication style. It must not change data access, permissions, safety controls, or external actions.

### 5. Confirmation after the model changes

After Arlo has adapted, show a lightweight message:

> “I’ve been more concise this week. Is that working better for you?”

Buttons should be simple:

- Yes, keep it.
- No, go back.
- Remind me later.

The user should not need to understand why the model made the change. A details option can explain the evidence for users who want it.

## Recording “all data” safely

A product can offer broad local capture only through a clearly named, explicit mode such as **Private Complete Context**. It must not mean covert or unlimited surveillance.

Requirements:

- A visible recording/collection indicator.
- One-tap pause from every screen.
- Device-level permission indicators for microphone, camera, screen, files, and accessibility data.
- A source list that says what is active in plain language.
- Short default raw-data retention, such as seven days, with user-adjustable limits.
- Derived memories stored separately from raw captures.
- Automatic deletion and secure wipe behavior.
- No cloud transfer in local-only mode.
- No collection from another person's account or device without their authorization.
- No access to email, screen, microphone, camera, or files until the user has explicitly enabled that category.

The app can make this easy by presenting one high-level choice, but the underlying policy still needs separate source boundaries so one accidental approval does not grant unlimited access.

## Personality boundaries

Arlo may learn how the user likes to communicate. It should not silently diagnose or classify the user. Sensitive characteristics must not be inferred and retained merely because a model guesses them from text, email, or screen content.

Personality memory should describe observable preferences:

- “Prefers concise explanations.”
- “Likes a direct recommendation before alternatives.”
- “Prefers reminders with a specific time.”

Every durable memory should remain editable, exportable, and deletable.

## Verification plan

- Confirm the app works without enabling any external source.
- Enable one source and verify the indicator appears immediately.
- Pause all collection and confirm no new events are recorded.
- Inspect local storage and confirm the raw payload is encrypted after vault integration.
- Test automatic style trials with synthetic text and verify rollback.
- Reject a trial and confirm the preference returns to its prior value.
- Allow a trial to expire and confirm it does not become permanent.
- Delete a source and verify its derived memories are marked for deletion or review.
- Attempt to trigger a cloud request in local-only mode and confirm it is blocked.
- Test a malicious instruction embedded in an email or screen capture and confirm it cannot change permissions or exfiltrate data.
