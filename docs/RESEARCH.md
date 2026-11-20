# Market & Product Research — Recurra

## Research Objective

Identify a genuine, everyday problem affecting Linux, Windows, and Android users where existing solutions are either:
- Closed-source and ironically charge a recurring subscription,
- Restricted to a single platform (e.g., iOS-only), or
- Require self-hosting a Docker web server with no native offline client.

## Candidates Evaluated

| Candidate | Problem | Existing FOSS Landscape | Cross-Platform Value | Verdict |
|---|---|---|---|---|
| **Recurra** (Offline Subscription, Recurring Bill & Warranty Vault) | Forgotten renewals, free-trial traps, annual billing spikes, and lost warranty dates | **Wallos** & **HomeBox** are great but require a self-hosted Docker server (web-only, no native offline desktop/Android app). **Bobby/Subtrack/Rocket Money** are proprietary/paid or iOS-only. | **Highest** — Native offline app on Linux, Windows, and Android with encrypted file backup for Syncthing/USB portability | **SELECTED** |
| LocalAPI (Offline REST Client) | Postman/Insomnia cloud lock-in | Bruno & Hoppscotch already dominate desktop; Android demand is niche | Medium | Rejected |
| FocusForge (Pomodoro/Time Tracker) | Toggl/Clockify SaaS lock-in | Super Productivity already covers Linux/Windows/Android well | Medium | Rejected |
| EnvVault (`.env` Manager) | Secret management across devices | KeePassXC/DX & Bitwarden already solve this thoroughly | Medium | Rejected |

## Why Recurra Wins

1. **Zero server required:** Works immediately out of the box on Linux, Windows 11, and Android.
2. **Real financial savings:** Catching even one forgotten $15/mo subscription or free trial pays for itself immediately.
3. **Combines two closely related personal vaults:** Recurring commitments (subscriptions/bills/trials) + hardware/asset warranties & receipts.
4. **True portability without cloud lock-in:** AES-256-GCM encrypted backups (`.recurra.enc`) + standard CSV export/import.
5. **Sustainable FOSS model:** Zero hosting cost, MIT licensed, supported via voluntary community donations (Buy Me a Coffee).
