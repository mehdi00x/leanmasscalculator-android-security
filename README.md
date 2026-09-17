# Android Security Hardening: OWASP MASVS

A Kotlin Android app (lean body mass calculator) used as a target for a **mobile security review based on OWASP MASVS**, followed by hardening of the weaknesses found.

## Security measures implemented

| Risk | Fix |
|---|---|
| Local database extraction through backups | Android backup disabled |
| Cleartext or intercepted traffic | HTTPS-only network config, trust limited to system CAs |
| Unattended open sessions | Session timeout after inactivity |
| Account enumeration | Generic authentication error messages |
| Invalid or malicious input | Strict input range validation |
| Compromised device | Root detection warning |

Full analysis: [`docs/security-writeup.md`](docs/security-writeup.md)

## Stack

Kotlin · Android SDK · Firebase Authentication & Firestore · SQLite · Gradle

## Run

1. Open the project in Android Studio.
2. Copy `app/google-services.example.json` to `app/google-services.json` and fill in your own Firebase config.
3. Build and run on an emulator or device.
