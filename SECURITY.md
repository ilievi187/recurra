# Security Policy

## Privacy & Security Guarantees

- **Offline-first by design:** Recurra makes zero network connections and collects zero telemetry.
- **Zero dangerous Android permissions:** The Android manifest requests no runtime permissions.
- **Authenticated encrypted backups:** Portable `.recurra.enc` backups use `AES-256-GCM` with `PBKDF2WithHmacSHA256` (120,000 iterations), a random 16-byte salt, and a random 12-byte nonce per export.

## Reporting a Vulnerability

If you discover a security issue (for example, in local storage handling, CSV parsing, or encrypted backup cryptography):

1. Please **do not** open a public issue with exploit details.
2. Use GitHub's **Private Vulnerability Reporting** ("Security" tab → "Report a vulnerability") on `ilievi187/recurra`.
3. Include steps to reproduce, affected platforms, and impact. We will acknowledge reports promptly and coordinate a fix.
