# Release Process

## Automated Release Pipeline

Tagging a semantic version (`vX.Y.Z`) and pushing it to GitHub triggers `.github/workflows/release.yml`:

1. **Linux & Android Job (`ubuntu-latest`):**
   - Runs desktop and Android unit tests.
   - Builds native Linux `.deb` package and Compose distributable.
   - Runs `./recurra --smoke-test` against the built Linux binary.
   - Packages portable `Recurra-<version>-x86_64.AppImage`.
   - Builds `recurra-<version>-android.apk`.
2. **Windows Job (`windows-latest`):**
   - Runs desktop unit tests on Windows.
   - Builds native Windows distributable and `.msi` installer.
   - Runs `Recurra.exe --smoke-test` against the built Windows binary.
   - Archives `Recurra-<version>-windows-x64-portable.zip`.
3. **Publish Job:**
   - Generates `SHA256SUMS.txt` across all verified artifacts.
   - Creates a GitHub Release with `CHANGELOG.md` notes and attaches all packages.

## Android Release Signing

By default in open-source CI without a private keystore secret, `assembleRelease` falls back safely to a debug-signed APK so anyone can build and install directly from GitHub Releases. To sign with a persistent release keystore, configure repository secrets `RECURRA_KEYSTORE_PATH`, `RECURRA_KEYSTORE_PASSWORD`, `RECURRA_KEY_ALIAS`, and `RECURRA_KEY_PASSWORD` — never commit `.jks` or `.keystore` files to Git.
