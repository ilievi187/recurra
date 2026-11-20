# Recurra Architecture

## Technology Stack

- **Language:** Kotlin 2.1.0 (Kotlin Multiplatform)
- **UI Framework:** Compose Multiplatform 1.7.3 + Material 3 (Skiko/Skia on Linux & Windows, native Jetpack Compose on Android)
- **Serialization & Dates:** `kotlinx.serialization` (JSON) & `kotlinx.datetime`
- **Cryptography:** Platform JVM/Android `javax.crypto` (`AES/GCM/NoPadding`, `PBKDF2WithHmacSHA256`, 120,000 iterations, random 16-byte salt & 12-byte IV)

## Module & Source-Set Layout

```
composeApp/src/
├── commonMain/   # ~95% shared code: domain models, finance engine, CSV codec, repository, ViewModel, Material 3 UI & charts
├── commonTest/   # Shared unit tests for financial math, CSV round-tripping, and repository CRUD/payment history
├── desktopMain/  # Linux & Windows entry point, XDG/%APPDATA% atomic file storage, AES-256-GCM crypto, keyboard shortcuts
├── desktopTest/  # Desktop JVM crypto & tamper-detection tests
└── androidMain/  # Android Activity (edge-to-edge), app-private storage, AES-256-GCM crypto, zero-permission manifest
```

## Storage & Data Safety

- **Linux:** `$XDG_DATA_HOME/recurra/vault.json` (defaults to `~/.local/share/recurra/vault.json`)
- **Windows:** `%APPDATA%\Recurra\vault.json`
- **Android:** `<app filesDir>/recurra/vault.json`
- Writes are performed atomically via a `.tmp` file + atomic move/rename to prevent corruption on crash or power loss.
- Exports (`.csv` and `.recurra.enc`) are written to the platform exports subfolder.

## Adaptive UI Strategy

- **Wide screens (Desktop / Tablets ≥ 780dp):** `NavigationRail` on the left, multi-column KPI grid, side-by-side dashboard cards, `Ctrl+N` shortcut for quick entry.
- **Compact screens (Android phones):** Bottom `NavigationBar`, 2x2 KPI grid, floating action button (`FAB`), edge-to-edge safe drawing insets.
