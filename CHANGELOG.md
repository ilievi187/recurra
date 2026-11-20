# Changelog

All notable changes to **Recurra** are documented in this file.

## [1.0.1] - 2026-11-20

### Changed
- Updated Buy Me a Coffee donation link to `https://buymeacoffee.com/aurorauap` across in-app Settings, `README.md`, and `.github/FUNDING.yml`.

## [1.0.0] - 2026-11-20

### Added
- **Cross-platform Kotlin + Compose Multiplatform application** for Linux, Windows 11, and Android.
- **Dashboard** with normalized monthly burn, yearly projection, 7-day / 30-day upcoming totals, active trial alerts, upcoming renewals list, and custom Canvas category donut chart.
- **Subscriptions & Recurring Bills** full CRUD, search, status/category filters, sorting, free-trial mode, auto-renew tracking, multi-currency offline conversion (12 currencies), and one-click **Mark Paid & Advance Cycle**.
- **Renewal Calendar** interactive monthly grid with category dots and daily breakdown.
- **Warranties & Assets Vault** tracking purchase dates, expiry countdown progress bars, serial numbers, and invoice references.
- **Analytics & Cashflow** 12-month projected cashflow bar chart highlighting annual renewal spikes, plus full payment history log.
- **AES-256-GCM Encrypted Backup & Restore** (`.recurra.enc` using PBKDF2WithHmacSHA256) and **RFC-4180 CSV Export/Import**.
- **Adaptive Material 3 UI** (Desktop `NavigationRail` + `Ctrl+N` shortcut; Android bottom `NavigationBar` + FAB; Light/Dark/System themes).
- **Packaging & CI/CD** for Linux (`.deb` + `.AppImage`), Windows 11 (`.msi` + portable `.zip`), and Android (`.apk`) with SHA-256 checksums.
