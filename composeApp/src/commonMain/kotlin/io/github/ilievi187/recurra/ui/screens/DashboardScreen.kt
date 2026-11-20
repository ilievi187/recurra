package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.UpcomingRenewal
import io.github.ilievi187.recurra.ui.components.CategoryDonutChart
import io.github.ilievi187.recurra.ui.state.RecurraUiState

@Composable
fun DashboardScreen(
    state: RecurraUiState,
    isWide: Boolean,
    onAddSubscription: () -> Unit,
    onLoadDemoData: () -> Unit,
    onMarkPaid: (String) -> Unit,
    onEditSubscription: (String) -> Unit,
    onOpenSubscriptions: () -> Unit,
) {
    val summary = state.summary
    val base = state.vault.settings.baseCurrency

    if (state.vault.subscriptions.isEmpty() && state.vault.warranties.isEmpty()) {
        EmptyDashboardState(
            onAddSubscription = onAddSubscription,
            onLoadDemoData = onLoadDemoData,
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Overview", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "${summary.activeCount} active · ${summary.pausedCount} paused · ${summary.activeWarrantiesCount} active warranties",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isWide) {
                    Button(onClick = onAddSubscription) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Subscription")
                    }
                }
            }
        }

        // KPI Row(s)
        item {
            val kpis = listOf(
                KpiData(
                    title = "Monthly Burn",
                    value = FinanceCalculator.formatCurrency(summary.monthlyTotal, base),
                    subtitle = "Normalized across all cycles",
                    icon = Icons.Rounded.Paid,
                    accent = MaterialTheme.colorScheme.primary,
                ),
                KpiData(
                    title = "Yearly Projection",
                    value = FinanceCalculator.formatCurrency(summary.yearlyTotal, base),
                    subtitle = "${summary.activeCount} active commitments",
                    icon = Icons.Rounded.TrendingUp,
                    accent = MaterialTheme.colorScheme.secondary,
                ),
                KpiData(
                    title = "Due Next 30 Days",
                    value = FinanceCalculator.formatCurrency(summary.dueNext30DaysTotal, base),
                    subtitle = "Next 7d: ${FinanceCalculator.formatCurrency(summary.dueNext7DaysTotal, base)}",
                    icon = Icons.Rounded.EventRepeat,
                    accent = Color(0xFF0D9488),
                ),
                KpiData(
                    title = "Trials & Warranties",
                    value = "${summary.trialCount} trials",
                    subtitle = "${summary.expiringWarrantiesSoonCount} warranties expiring <60d",
                    icon = if (summary.trialCount > 0) Icons.Rounded.WarningAmber else Icons.Rounded.Shield,
                    accent = if (summary.trialCount > 0) Color(0xFFEA580C) else Color(0xFF16A34A),
                ),
            )

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    kpis.forEach { kpi ->
                        KpiCard(kpi, modifier = Modifier.weight(1f))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(kpis[0], modifier = Modifier.weight(1f))
                        KpiCard(kpis[1], modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(kpis[2], modifier = Modifier.weight(1f))
                        KpiCard(kpis[3], modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Main content: Upcoming + Category Breakdown
        item {
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    UpcomingRenewalsCard(
                        upcoming = summary.upcomingRenewals.take(7),
                        baseCurrency = base,
                        onMarkPaid = onMarkPaid,
                        onEdit = onEditSubscription,
                        onViewAll = onOpenSubscriptions,
                        modifier = Modifier.weight(1.25f),
                    )
                    SectionCard(
                        title = "Spend by Category",
                        modifier = Modifier.weight(1f),
                    ) {
                        CategoryDonutChart(
                            breakdown = summary.categoryBreakdown,
                            baseCurrency = base,
                            monthlyTotal = summary.monthlyTotal,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UpcomingRenewalsCard(
                        upcoming = summary.upcomingRenewals.take(6),
                        baseCurrency = base,
                        onMarkPaid = onMarkPaid,
                        onEdit = onEditSubscription,
                        onViewAll = onOpenSubscriptions,
                    )
                    SectionCard(title = "Spend by Category") {
                        CategoryDonutChart(
                            breakdown = summary.categoryBreakdown,
                            baseCurrency = base,
                            monthlyTotal = summary.monthlyTotal,
                        )
                    }
                }
            }
        }
    }
}

private data class KpiData(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
private fun KpiCard(data: KpiData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(data.icon, contentDescription = null, tint = data.accent, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = data.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                action?.invoke()
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun UpcomingRenewalsCard(
    upcoming: List<UpcomingRenewal>,
    baseCurrency: String,
    onMarkPaid: (String) -> Unit,
    onEdit: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = "Upcoming Renewals",
        modifier = modifier,
        action = {
            TextButton(onClick = onViewAll) { Text("View all") }
        },
    ) {
        if (upcoming.isEmpty()) {
            Text(
                text = "No upcoming renewals scheduled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                upcoming.forEach { item ->
                    UpcomingRow(
                        item = item,
                        baseCurrency = baseCurrency,
                        onMarkPaid = { onMarkPaid(item.subscription.id) },
                        onEdit = { onEdit(item.subscription.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingRow(
    item: UpcomingRenewal,
    baseCurrency: String,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
) {
    val urgencyColor = when {
        item.isTrialEnding -> Color(0xFFEA580C)
        item.daysUntilDue <= 0 -> MaterialTheme.colorScheme.error
        item.daysUntilDue <= 5 -> Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.primary
    }
    val dueBadge = when {
        item.daysUntilDue < 0 -> "Overdue ${-item.daysUntilDue}d"
        item.daysUntilDue == 0 -> "Due today"
        item.daysUntilDue == 1 -> "Tomorrow"
        else -> "In ${item.daysUntilDue}d"
    }

    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(item.subscription.category.colorHex)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (item.isTrialEnding) {
                        Surface(
                            color = Color(0xFFEA580C).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = "TRIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEA580C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    text = "${item.dueDate} · ${item.subscription.cycle.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FinanceCalculator.formatCurrency(item.subscription.amount, item.subscription.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dueBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onMarkPaid, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Mark paid", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(
    onAddSubscription: () -> Unit,
    onLoadDemoData: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(0.95f),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.EventRepeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
                Text("Welcome to Recurra", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Track subscriptions, recurring bills, free trials, and product warranties 100% offline. Your financial data never leaves your device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onAddSubscription) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add First Subscription")
                    }
                    OutlinedButton(onClick = onLoadDemoData) {
                        Text("Explore Sample Data")
                    }
                }
            }
        }
    }
}
