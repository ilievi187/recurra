package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.WarrantyItem
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import kotlinx.datetime.daysUntil

@Composable
fun WarrantiesScreen(
    state: RecurraUiState,
    onAdd: () -> Unit,
    onEdit: (WarrantyItem) -> Unit,
    onDelete: (String) -> Unit,
) {
    val today = FinanceCalculator.today()
    val sorted = state.vault.warranties.sortedBy { it.expiryDate }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Warranties & Assets", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${sorted.size} tracked item(s) · receipts, serials & expiry dates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Warranty")
            }
        }

        if (sorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                    Text("No warranties saved yet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Store hardware warranties, serial numbers, and invoice references offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sorted, key = { it.id }) { item ->
                    val purchase = FinanceCalculator.parseDateOrNull(item.purchaseDate)
                    val expiry = FinanceCalculator.parseDateOrNull(item.expiryDate)
                    val daysLeft = if (expiry != null) today.daysUntil(expiry) else 0
                    val totalDays = if (purchase != null && expiry != null) purchase.daysUntil(expiry).coerceAtLeast(1) else 365
                    val remainingRatio = (daysLeft.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

                    val statusColor = when {
                        daysLeft < 0 -> MaterialTheme.colorScheme.error
                        daysLeft <= 60 -> Color(0xFFD97706)
                        else -> Color(0xFF16A34A)
                    }
                    val statusLabel = when {
                        daysLeft < 0 -> "Expired ${-daysLeft}d ago"
                        daysLeft == 0 -> "Expires today"
                        else -> "$daysLeft days remaining"
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, style = MaterialTheme.typography.titleMedium)
                                    val meta = listOf(item.brand, item.vendor).filter { it.isNotBlank() }.joinToString(" · ")
                                    if (meta.isNotBlank()) {
                                        Text(meta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(
                                    color = statusColor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { remainingRatio },
                                color = statusColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Purchased: ${item.purchaseDate} → Expires: ${item.expiryDate}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (item.purchasePrice > 0.0) {
                                    Text(
                                        text = FinanceCalculator.formatCurrency(item.purchasePrice, item.currency),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            if (item.serialNumber.isNotBlank() || item.receiptReference.isNotBlank()) {
                                Text(
                                    text = listOfNotNull(
                                        item.serialNumber.takeIf { it.isNotBlank() }?.let { "S/N: $it" },
                                        item.receiptReference.takeIf { it.isNotBlank() }?.let { "Receipt: $it" },
                                    ).joinToString("  |  "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            if (item.notes.isNotBlank()) {
                                Text(item.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { onEdit(item) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onDelete(item.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
