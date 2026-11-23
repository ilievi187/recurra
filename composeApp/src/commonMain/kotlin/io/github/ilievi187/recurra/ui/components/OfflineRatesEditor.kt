package io.github.ilievi187.recurra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.DefaultCurrencies
import io.github.ilievi187.recurra.domain.OfflineExchangeRates

/**
 * Every quote is entered in the currently selected base currency, and stored
 * locally using the vault's existing USD-anchored map. No online rate service.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfflineRatesEditor(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
) {
    val base = settings.baseCurrency.uppercase()
    var currency by remember(base) {
        mutableStateOf(DefaultCurrencies.supported.first { it.code != base }.code)
    }
    var rateText by remember(base, currency, settings.ratesToUsd) {
        mutableStateOf(OfflineExchangeRates.rateInBase(settings, currency).toString())
    }
    var confirmReset by remember { mutableStateOf(false) }

    val parsed = rateText.trim().replace(',', '.').toDoubleOrNull()
    val valid = parsed != null && parsed.isFinite() && parsed > 0.0
    val original = OfflineExchangeRates.rateInBase(settings, currency)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Set manual exchange rates for displaying totals in $base. " +
                "Rates are saved on this device and never fetched online.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("Select a currency", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DefaultCurrencies.supported
                .filter { it.code != base }
                .forEach { option ->
                    FilterChip(
                        selected = currency == option.code,
                        onClick = { currency = option.code },
                        label = { Text(option.code) },
                    )
                }
        }

        OutlinedTextField(
            value = rateText,
            onValueChange = { rateText = it },
            label = { Text("1 $currency = ? $base") },
            supportingText = {
                Text(
                    if (rateText.isBlank() || !valid) {
                        "Enter a positive number (for example, 1.08)."
                    } else {
                        "1 $currency = $rateText $base"
                    },
                )
            },
            isError = !valid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (currency == "USD" && base != "USD") {
            Text(
                text = "Updating USD recalibrates the base-currency anchor while preserving " +
                    "the other displayed cross-rates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                enabled = valid && parsed != original,
                onClick = {
                    if (parsed != null && parsed.isFinite() && parsed > 0.0) {
                        onUpdateSettings(
                            OfflineExchangeRates.withRate(settings, currency, parsed),
                        )
                    }
                },
            ) {
                Text("Save exchange rate")
            }
            if (confirmReset) {
                OutlinedButton(
                    onClick = {
                        onUpdateSettings(settings.copy(ratesToUsd = DefaultCurrencies.defaultRatesToUsd))
                        confirmReset = false
                    },
                ) {
                    Text("Confirm reset")
                }
                TextButton(onClick = { confirmReset = false }) {
                    Text("Cancel")
                }
            } else {
                TextButton(onClick = { confirmReset = true }) {
                    Text("Reset all rates")
                }
            }
        }
        Text(
            text = "Default rates are illustrative, not live market quotes. " +
                "Check and update them manually before relying on converted amounts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
