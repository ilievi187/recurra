package io.github.ilievi187.recurra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.BillingCycle
import io.github.ilievi187.recurra.domain.DefaultCurrencies
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import io.github.ilievi187.recurra.domain.SubscriptionStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionDialog(
    initial: Subscription?,
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSave: (Subscription) -> Unit,
    onScanInvoice: (() -> Unit)? = null,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var amountText by remember { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var currency by remember { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var cycle by remember { mutableStateOf(initial?.cycle ?: BillingCycle.MONTHLY) }
    var customDaysText by remember { mutableStateOf((initial?.customCycleDays ?: 30).toString()) }
    var nextRenewalDate by remember {
        mutableStateOf(initial?.nextRenewalDate ?: FinanceCalculator.today().toString())
    }
    var category by remember { mutableStateOf(initial?.category ?: SubscriptionCategory.OTHER) }
    var isFreeTrial by remember { mutableStateOf(initial?.isFreeTrial ?: false) }
    var trialEndDate by remember { mutableStateOf(initial?.trialEndDate ?: nextRenewalDate) }
    var autoRenew by remember { mutableStateOf(initial?.autoRenew ?: true) }
    var paymentMethod by remember { mutableStateOf(initial?.paymentMethod.orEmpty()) }
    var websiteUrl by remember { mutableStateOf(initial?.websiteUrl.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    initial == null -> "Add Subscription"
                    initial.id.isBlank() -> "Review Scanned Bill"
                    else -> "Edit Subscription"
                },
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (onScanInvoice != null && (initial == null || initial.id.isBlank())) {
                    FilledTonalButton(
                        onClick = onScanInvoice,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Photo or PDF Invoice to Auto-Fill")
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Service / Bill Name *") },
                    placeholder = { Text("e.g., Proton Unlimited, Hetzner VPS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorText = null },
                        label = { Text("Amount *") },
                        placeholder = { Text("9.99") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = nextRenewalDate,
                        onValueChange = { nextRenewalDate = it; errorText = null },
                        label = { Text("Next Renewal (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                    )
                }

                Text("Currency", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DefaultCurrencies.supported.take(8).forEach { cur ->
                        FilterChip(
                            selected = currency.equals(cur.code, ignoreCase = true),
                            onClick = { currency = cur.code },
                            label = { Text("${cur.code} (${cur.symbol})") },
                        )
                    }
                }

                Text("Billing Cycle", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BillingCycle.entries.forEach { option ->
                        FilterChip(
                            selected = cycle == option,
                            onClick = { cycle = option },
                            label = { Text(option.displayName) },
                        )
                    }
                }

                if (cycle == BillingCycle.CUSTOM_DAYS) {
                    OutlinedTextField(
                        value = customDaysText,
                        onValueChange = { customDaysText = it },
                        label = { Text("Repeat every N days") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text("Category", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SubscriptionCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Free Trial (alert before conversion)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isFreeTrial, onCheckedChange = { isFreeTrial = it })
                }

                if (isFreeTrial) {
                    OutlinedTextField(
                        value = trialEndDate,
                        onValueChange = { trialEndDate = it },
                        label = { Text("Trial End Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Auto-renew enabled", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = autoRenew, onCheckedChange = { autoRenew = it })
                }

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") },
                    placeholder = { Text("Visa •••• 4821, PayPal, SEPA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Manage / Cancel URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val parsedAmount = amountText.trim().replace(",", ".").toDoubleOrNull()
                    val parsedDate = FinanceCalculator.parseDateOrNull(nextRenewalDate)
                    val parsedTrial = if (isFreeTrial) FinanceCalculator.parseDateOrNull(trialEndDate) else null

                    when {
                        trimmedName.isEmpty() -> errorText = "Please enter a name."
                        parsedAmount == null || parsedAmount < 0.0 -> errorText = "Enter a valid non-negative amount."
                        parsedDate == null -> errorText = "Next renewal date must be YYYY-MM-DD."
                        isFreeTrial && parsedTrial == null -> errorText = "Trial end date must be YYYY-MM-DD."
                        else -> {
                            onSave(
                                Subscription(
                                    id = initial?.id.orEmpty(),
                                    name = trimmedName,
                                    amount = parsedAmount,
                                    currency = currency.uppercase(),
                                    cycle = cycle,
                                    customCycleDays = customDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 30,
                                    nextRenewalDate = parsedDate.toString(),
                                    category = category,
                                    status = initial?.status ?: SubscriptionStatus.ACTIVE,
                                    isFreeTrial = isFreeTrial,
                                    trialEndDate = if (isFreeTrial) parsedTrial.toString() else null,
                                    autoRenew = autoRenew,
                                    paymentMethod = paymentMethod.trim(),
                                    websiteUrl = websiteUrl.trim(),
                                    notes = notes.trim(),
                                    createdAt = initial?.createdAt.orEmpty(),
                                ),
                            )
                        }
                    }
                },
            ) {
                Text(if (initial == null || initial.id.isBlank()) "Save" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
