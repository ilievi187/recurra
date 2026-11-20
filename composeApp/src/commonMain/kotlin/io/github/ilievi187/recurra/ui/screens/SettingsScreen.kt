package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.data.VaultStorage
import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.DefaultCurrencies
import io.github.ilievi187.recurra.domain.ThemePreference
import io.github.ilievi187.recurra.ui.state.RecurraUiState

private const val BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/aurorauap"
private const val GITHUB_REPO_URL = "https://github.com/ilievi187/recurra"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: RecurraUiState,
    onUpdateSettings: (AppSettings) -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: (String) -> Unit,
    onExportEncryptedBackup: (String) -> Unit,
    onRestoreEncryptedBackup: (String, String) -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val settings = state.vault.settings
    var backupPassphrase by remember { mutableStateOf("") }
    var importFilePath by remember { mutableStateOf(state.exportFiles.firstOrNull().orEmpty()) }
    var restorePassphrase by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text("Settings & Privacy", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Local storage: ${VaultStorage.vaultDirectoryPath()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Appearance & Currency
        item {
            SectionCard(title = "Appearance & Base Currency") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePreference.entries.forEach { pref ->
                            FilterChip(
                                selected = settings.themePreference == pref,
                                onClick = { onUpdateSettings(settings.copy(themePreference = pref)) },
                                label = { Text(pref.displayName) },
                            )
                        }
                    }

                    Text("Display Base Currency (all totals convert offline)", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DefaultCurrencies.supported.forEach { cur ->
                            FilterChip(
                                selected = settings.baseCurrency.equals(cur.code, ignoreCase = true),
                                onClick = { onUpdateSettings(settings.copy(baseCurrency = cur.code)) },
                                label = { Text("${cur.code} (${cur.symbol})") },
                            )
                        }
                    }
                }
            }
        }

        // AES-256-GCM Encrypted Backup & CSV
        item {
            SectionCard(title = "Encrypted Backup (AES-256-GCM) & CSV") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Create passphrase-protected backups you can safely sync via Syncthing, USB, or cloud storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = backupPassphrase,
                            onValueChange = { backupPassphrase = it },
                            label = { Text("Backup Passphrase (min 6 chars)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                onExportEncryptedBackup(backupPassphrase)
                                backupPassphrase = ""
                            },
                            enabled = backupPassphrase.length >= 6,
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export Encrypted")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onExportCsv) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export Subscriptions CSV")
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Restore from Encrypted Backup or Import CSV", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = importFilePath,
                        onValueChange = { importFilePath = it },
                        label = { Text("Backup (.recurra.enc) or CSV file path") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.exportFiles.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            state.exportFiles.take(5).forEach { path ->
                                val short = path.substringAfterLast('/').substringAfterLast('\\')
                                FilterChip(
                                    selected = importFilePath == path,
                                    onClick = { importFilePath = path },
                                    label = { Text(short) },
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = restorePassphrase,
                            onValueChange = { restorePassphrase = it },
                            label = { Text("Passphrase (for .recurra.enc)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                if (importFilePath.endsWith(".csv", ignoreCase = true)) {
                                    onImportCsv(importFilePath)
                                } else {
                                    onRestoreEncryptedBackup(importFilePath, restorePassphrase)
                                    restorePassphrase = ""
                                }
                            },
                            enabled = importFilePath.isNotBlank(),
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Import / Restore")
                        }
                    }
                }
            }
        }

        // Data Management
        item {
            SectionCard(title = "Data Management") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onLoadDemoData) {
                        Text("Load Sample Data")
                    }
                    if (!confirmClear) {
                        TextButton(onClick = { confirmClear = true }) {
                            Text("Clear All Local Data", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Button(onClick = {
                            onClearAllData()
                            confirmClear = false
                        }) {
                            Text("Confirm Delete All")
                        }
                        TextButton(onClick = { confirmClear = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        // Voluntary FOSS Support / Buy Me a Coffee
        item {
            SectionCard(title = "Support Recurra Development") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recurra is 100% Free & Open Source (MIT License) with zero ads, zero telemetry, and zero paywalls. Every feature is unlocked for everyone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "If Recurra helped you catch a forgotten renewal or free trial, consider buying the developer a coffee!",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { onOpenUrl(BUY_ME_A_COFFEE_URL) }) {
                            Icon(Icons.Rounded.Coffee, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Buy Me a Coffee")
                        }
                        OutlinedButton(onClick = { onOpenUrl(GITHUB_REPO_URL) }) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Source on GitHub")
                        }
                    }
                }
            }
        }
    }
}
