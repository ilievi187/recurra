package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import io.github.ilievi187.recurra.domain.SubscriptionStatus
import io.github.ilievi187.recurra.ui.components.CategoryAvatar
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import io.github.ilievi187.recurra.ui.state.SubscriptionSort

@Composable
fun SubscriptionsScreen(
    state: RecurraUiState,
    isWide: Boolean,
    onSearchChange: (String) -> Unit,
    onStatusFilter: (SubscriptionStatus?) -> Unit,
    onCategoryFilter: (SubscriptionCategory?) -> Unit,
    onSortChange: (SubscriptionSort) -> Unit,
    onAdd: () -> Unit,
    onScanInvoice: () -> Unit,
    onEdit: (Subscription) -> Unit,
    onMarkPaid: (String) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val base = state.vault.settings.baseCurrency
    val rates = state.vault.settings.ratesToUsd

    val filtered = state.vault.subscriptions
        .filter { sub ->
            val matchesStatus = state.selectedStatus == null || sub.status == state.selectedStatus
            val matchesCat = state.selectedCategory == null || sub.category == state.selectedCategory
            val q = state.searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                sub.name.lowercase().contains(q) ||
                sub.notes.lowercase().contains(q) ||
                sub.paymentMethod.lowercase().contains(q) ||
                sub.category.displayName.lowercase().contains(q)
            matchesStatus && matchesCat && matchesQuery
        }
        .let { list ->
            when (state.sortBy) {
                SubscriptionSort.NEXT_RENEWAL -> list.sortedBy { it.nextRenewalDate }
                SubscriptionSort.AMOUNT_DESC -> list.sortedByDescending {
                    FinanceCalculator.convertAmount(
                        FinanceCalculator.normalizedMonthlyAmount(it),
                        it.currency,
                        base,
                        rates,
                    )
                }
                SubscriptionSort.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            }
        }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Subscriptions & Bills", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${filtered.size} shown of ${state.vault.subscriptions.size} total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onScanInvoice) {
                    Icon(Icons.Rounded.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Scan Invoice")
                }
                if (isWide) {
                    Button(onClick = onAdd) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Bill")
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = { Text("Search by name, payment method, category, or note...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        // Status & Sort Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = state.selectedStatus == null,
                onClick = { onStatusFilter(null) },
                label = { Text("All") },
            )
            SubscriptionStatus.entries.forEach { st ->
                FilterChip(
                    selected = state.selectedStatus == st,
                    onClick = { onStatusFilter(st) },
                    label = { Text(st.displayName) },
                )
            }
            Spacer(Modifier.width(8.dp))
            SubscriptionSort.entries.forEach { s ->
                FilterChip(
                    selected = state.sortBy == s,
                    onClick = { onSortChange(s) },
                    label = { Text("Sort: ${s.label}") },
                )
            }
        }

        // Category Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { onCategoryFilter(null) },
                label = { Text("All Categories") },
            )
            SubscriptionCategory.entries.forEach { cat ->
                FilterChip(
                    selected = state.selectedCategory == cat,
                    onClick = { onCategoryFilter(cat) },
                    label = { Text(cat.displayName) },
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No matching subscriptions found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { sub ->
                    val monthlyEq = FinanceCalculator.convertAmount(
                        FinanceCalculator.normalizedMonthlyAmount(sub),
                        sub.currency,
                        base,
                        rates,
                    )
                    SubscriptionItemCard(
                        sub = sub,
                        monthlyEquivalentLabel = "${FinanceCalculator.formatCurrency(monthlyEq, base)}/mo",
                        onEdit = { onEdit(sub) },
                        onMarkPaid = { onMarkPaid(sub.id) },
                        onToggleStatus = { onToggleStatus(sub.id) },
                        onDelete = { onDelete(sub.id) },
                        onOpenUrl = if (sub.websiteUrl.isNotBlank()) ({ onOpenUrl(sub.websiteUrl) }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionItemCard(
    sub: Subscription,
    monthlyEquivalentLabel: String,
    onEdit: () -> Unit,
    onMarkPaid: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onOpenUrl: (() -> Unit)?,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryAvatar(category = sub.category, size = 40.dp, iconSize = 20.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(sub.name, style = MaterialTheme.typography.titleMedium)
                        if (sub.isFreeTrial) {
                            BadgePill("TRIAL", Color(0xFFEA580C))
                        }
                        if (sub.status != SubscriptionStatus.ACTIVE) {
                            BadgePill(sub.status.displayName.uppercase(), MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = buildString {
                            append(sub.category.displayName)
                            append(" · Next: ")
                            append(sub.nextRenewalDate)
                            if (sub.paymentMethod.isNotBlank()) {
                                append(" · ")
                                append(sub.paymentMethod)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${FinanceCalculator.formatCurrency(sub.amount, sub.currency)} ${sub.cycle.shortLabel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "≈ $monthlyEquivalentLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (sub.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = sub.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onOpenUrl != null) {
                    IconButton(onClick = onOpenUrl) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Open website", modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onMarkPaid) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Mark paid & advance cycle", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onToggleStatus) {
                    Icon(
                        imageVector = if (sub.status == SubscriptionStatus.ACTIVE) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                        contentDescription = "Pause or resume",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
