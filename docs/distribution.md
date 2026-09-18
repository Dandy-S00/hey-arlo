# Arlo production distribution

The repository now supports packaging the existing web app as:

- Android release APK through Capacitor
- Windows NSIS installer through Electron Builder
- Linux AppImage and Debian package through Electron Builder
- macOS DMG through Electron Builder

## Production Android signing

A new production keystore must be generated once and stored as GitHub Actions secrets. Never commit the keystore or passwords.

Required repository secrets:

- `ARLO_KEYSTORE_BASE64`
- `ARLO_KEYSTORE_PASSWORD`
- `ARLO_KEY_ALIAS`
- `ARLO_KEY_PASSWORD`

The workflow intentionally fails instead of producing an unsigned release when the keystore is not configured.

## Website downloads

The download portal should be hosted at `arlo.v3rcy.com`. Publish the generated artifacts under versioned paths and provide separate buttons for Android, Windows, Linux, and macOS. iOS should link to the App Store or TestFlight after Apple signing and review; a raw production IPA should not be offered as a general public download.

## Important limitation

This packages the current Arlo web application. It does not add native-only capabilities or replace the requirement for Apple Developer signing, store metadata, privacy disclosures, and platform-specific QA.
