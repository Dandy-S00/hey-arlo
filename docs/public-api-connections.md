# Public API connections

Arlo now exposes a permission-center prototype for discoverable API connections.

## User experience

- Users can choose an API by name from a catalog with a recognizable short logo mark.
- Users can ask for a connection in natural language, such as “connect Google Calendar”.
- Unknown names fall back to an “Any other API” path rather than silently guessing.
- Every connection starts in an explicit awaiting-provider-auth state.

## Security boundary

The catalog contains no secrets and stores only consent metadata in session storage. It does not claim that a provider is connected until a real provider adapter completes OAuth or credential setup.

The next implementation phase is a secure adapter layer: OAuth redirect handling for each provider, encrypted credential storage through Arlo’s vault, scoped actions, token rotation, and a generic API manifest for user-supplied public APIs. Browser-only API calls must also pass a CORS review; arbitrary API keys must never be embedded in the frontend bundle.
