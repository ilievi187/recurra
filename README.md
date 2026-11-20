# Recurra

**Privacy-first, offline-first subscription, recurring bill, free-trial, and warranty tracker for Linux, Windows 11, and Android.**

[![License: MIT](https://img.shields.io/badge/License-MIT-0284C7.svg)](LICENSE)
[![CI](https://github.com/ilievi187/recurra/actions/workflows/ci.yml/badge.svg)](https://github.com/ilievi187/recurra/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/ilievi187/recurra)](https://github.com/ilievi187/recurra/releases/latest)
[![Buy Me a Coffee](https://img.shields.io/badge/Support-Buy%20Me%20a%20Coffee-FFDD00?logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/aurorauap)

---

## Why Recurra?

Tracking recurring expenses shouldn't require paying another recurring subscription, handing your financial data to a cloud broker, or maintaining a Docker server just to check when a free trial ends.

**Recurra** runs **100% natively and offline** on **Linux**, **Windows 11**, and **Android** from a single shared Kotlin + Compose Multiplatform codebase:

- **Zero accounts, zero cloud lock-in, zero telemetry**
- **Zero dangerous Android permissions**
- **Every feature unlocked for everyone** (MIT License)

---

## Key Features

- **Financial Dashboard:** Normalized monthly burn, yearly projection, 7-day & 30-day due totals, active free-trial alerts, and a custom Canvas category spend donut chart.
- **Subscriptions & Recurring Bills:** Full CRUD, search, category/status filters, sorting, weekly/bi-weekly/monthly/quarterly/semi-annual/yearly/custom-day cycles, free-trial countdowns, and one-click **Mark Paid & Advance Cycle**.
- **Multi-Currency Offline Conversion:** Track bills in 12 currencies (USD, EUR, GBP, BGN, CHF, CAD, AUD, JPY, PLN, SEK, INR, BRL) automatically normalized into your base currency.
- **Interactive Renewal Calendar:** Month-by-month grid showing category indicators and daily renewal totals.
- **Warranties & Assets Vault:** Track purchase dates, warranty expiration countdown bars, serial numbers, vendors, and invoice references.
- **12-Month Cashflow Analytics:** Spot annual renewal spikes before they hit your card, plus a full payment history log.
- **AES-256-GCM Encrypted Backups & CSV:** Export passphrase-protected `.recurra.enc` backups (PBKDF2WithHmacSHA256 + AES-256-GCM) to sync safely via Syncthing/USB, or export/import standard CSV files.
- **Adaptive Material 3 UI:** Desktop `NavigationRail` with `Ctrl+N` quick-add shortcut, Android bottom `NavigationBar` + FAB, Light/Dark/System themes.

---

## Download & Installation

Download the latest verified packages from [**GitHub Releases**](https://github.com/ilievi187/recurra/releases/latest) and verify integrity with `SHA256SUMS.txt`.

### Linux
- **AppImage (recommended for any distro):**
  ```bash
  chmod +x Recurra-1.0.0-x86_64.AppImage
  ./Recurra-1.0.0-x86_64.AppImage
  ```
- **Debian / Ubuntu (`.deb`):**
  ```bash
  sudo apt install ./recurra-1.0.0-amd64.deb
  ```

### Windows 11
- **Installer:** Download and run `Recurra-1.0.0-windows-x64.msi`.
- **Portable:** Extract `Recurra-1.0.0-windows-x64-portable.zip` and launch `Recurra.exe`.

### Android (Direct APK — No Google Play Required)
1. Download `recurra-1.0.0-android.apk` from [GitHub Releases](https://github.com/ilievi187/recurra/releases/latest) on your Android device.
2. Open the downloaded `.apk` file. If prompted by Android, enable **"Install unknown apps"** for your browser or file manager.
3. Tap **Install**. Recurra requests **zero runtime permissions** and works completely offline.

---

## Building from Source

### Prerequisites
- **JDK 21**
- **Gradle 8.11.1+**
- **Android SDK (API 35)** *(only required for Android builds)*

### Commands
```bash
# Run unit tests
gradle :composeApp:desktopTest

# Run desktop app (Linux / Windows)
gradle :composeApp:run

# Build Linux DEB & distributable
gradle :composeApp:packageDeb :composeApp:createDistributable

# Build Linux AppImage
./packaging/linux/build-appimage.sh 1.0.0

# Build Windows MSI & distributable (on Windows)
gradle :composeApp:packageMsi :composeApp:createDistributable

# Build Android APK
gradle :composeApp:assembleRelease
```

---

## Documentation

- [Market Research & Product Selection](docs/RESEARCH.md)
- [Architecture Overview](docs/ARCHITECTURE.md)
- [Release Process](docs/RELEASE.md)
- [Contributing Guide](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)

---

## Support Development

Recurra is completely free and open source — no paywalls, no ads, no feature gating. If it helped you catch a forgotten renewal or free trial, voluntary donations are warmly appreciated:

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-Support%20Recurra-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/aurorauap)

---

## License

Licensed under the [MIT License](LICENSE).
