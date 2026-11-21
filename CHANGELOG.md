# Changelog

All notable changes to **Recurra** are documented in this file.

## [1.5.0] - 2026-11-21

### Improved
- **Beginner-Friendly Onboarding & 1-Click Scanning Everywhere:** Added a prominent **Scan Photo / PDF** button directly on the Welcome screen, Dashboard hero card, and inside both the **Add Bill** and **Add Warranty** popups so users can auto-fill from a receipt or invoice immediately.
- **1-Click Clear Demo Data Banner:** When exploring sample data, a clear banner appears right at the top of the Dashboard with a single **Clear Demo Data** button — no need to hunt through Settings.

## [1.4.1] - 2026-11-21

### Fixed
- **Hyprland / Wayland crash when clicking Scan Receipt/Invoice:** Replaced off-EDT `java.awt.FileDialog` on Linux with native Wayland-safe `zenity` / `kdialog` portal file pickers (with EDT-safe `JFileChooser` fallback) and ensured Windows/macOS `FileDialog` runs strictly on the AWT EDT.

## [1.4.0] - 2026-11-21

### Added
- **Offline Receipt, Invoice & Warranty Scanner:** Pick any receipt photo (`JPG`/`PNG`/`WEBP`) or `PDF` invoice to automatically extract vendor, product/plan name, total amount & currency, purchase date, warranty period/expiry date, invoice number, and serial number — supporting both **Bulgarian (Cyrillic)** and **English** documents!
- **Cross-platform Offline OCR & PDF Extraction:** Uses **Apache PDFBox** (embedded, 100% offline) + **Tesseract OCR** (`bul+eng`) on Linux & Windows 11, and **Google ML Kit on-device OCR** (zero extra permissions via Android Storage Access Framework) on Android.
- **Local Receipt Attachments:** Scanned receipts and PDFs are archived locally inside Recurra's attachments folder and can be reopened directly from saved warranties.

## [1.3.0] - 2026-11-21

### Changed
- **Warm Botanical Material You Design (Google Calendar–inspired):** Default warm cream (`#FAF9F4`), sage green (`#2E7D5B`), soft mint, and warm butter-yellow palette designed to feel inviting and human-friendly (with a cozy warm-forest dark mode option in Settings).
- **Illustrated Seasonal Botanical Artwork:** Procedural Compose Canvas illustrations with rolling sunlit hills, warm sun, soft clouds, leaves, and blooming seasonal flowers on both the Calendar header and Dashboard overview.
- **Friendly Everyday Copy:** Updated labels and empty states for a warm, approachable everyday experience.

## [1.2.0] - 2026-11-20

### Changed
- **Complete UI/UX visual redesign ("Obsidian Aurora" & "Warm Alabaster"):** Replaced the plain blue theme with an emerald, violet, and amber fintech palette, layered surfaces, and subtle ambient gradient hero cards.
- **Visual Renewal Calendar:** Added seasonal gradient header banner with ambient glow and seasonal icon, category icon badges and daily totals directly inside calendar day cells, animated day selection, and one-click Mark Paid from the calendar.
- **Smooth Animations & Micro-interactions:** Added animated screen transitions (`AnimatedContent`), animated currency counters, animated donut chart sweep and gradient cashflow bars, and tinted squircle category icon avatars across all screens.

## [1.1.0] - 2026-11-20

### Added
- **Universal Linux distro packaging:** Added native `.rpm` (Fedora/openSUSE), portable `.tar.gz` (NixOS/Void/Gentoo), and Arch Linux `packaging/arch/PKGBUILD` alongside `.AppImage` and `.deb`.
- **Wayland, Hyprland & tiling-WM auto-compatibility:** `AppRun` and `launch-recurra.sh` automatically configure `_JAVA_AWT_WM_NONREPARENTING=1`, system font anti-aliasing, and smart XWayland socket fallback (e.g. `/tmp/.X11-unix/X0_`).
- **AppStream metadata** (`io.github.ilievi187.recurra.metainfo.xml`) for Linux software centers and AppImage integration.
- **Native desktop window icon** embedded in Compose Desktop builds.
- **Linux desktop renewal notifications** via `notify-send` when subscriptions or trials are due within 3 days.

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
