# Contributing to Recurra

Thank you for your interest in contributing to **Recurra**!

## How to Contribute

1. **Fork** the repository and create a feature branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. **Build & test locally** (requires JDK 21 and Gradle 8.11.1+):
   ```bash
   gradle :composeApp:desktopTest
   gradle :composeApp:run
   ```
3. **Follow project principles**:
   - Keep Recurra **100% offline-first** with zero telemetry, ads, or paywalls.
   - Do not add unnecessary permissions (especially on Android).
   - Keep business logic inside `commonMain` (`domain/` and `data/`) with unit tests in `commonTest`.
   - Ensure UI changes work on both desktop wide screens and Android compact screens.
4. **Open a Pull Request** targeting `main` with a clear description and test evidence.

## Code Style

- Follow standard Kotlin official code style (`kotlin.code.style=official`).
- Keep commits small, focused, and written in imperative mood (`feat:`, `fix:`, `docs:`, `test:`).
