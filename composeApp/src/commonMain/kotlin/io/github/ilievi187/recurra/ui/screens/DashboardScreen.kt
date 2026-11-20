package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import io.github.ilievi187.recurra.domain.ThemePreference
import io.github.ilievi187.recurra.domain.UpcomingRenewal
import io.github.ilievi187.recurra.ui.components.AmbientOrbsBackdrop
import io.github.ilievi187.recurra.ui.components.AnimatedCurrencyText
import io.github.ilievi187.recurra.ui.components.CategoryAvatar
import io.github.ilievi187.recurra.ui.components.CategoryDonutChart
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import io.github.ilievi187.recurra.ui.theme.RecurraPalette

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
    val isDark = when (state.vault.settings.themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    if (state.vault.subscriptions.isEmpty() && state.vault.warranties.isEmpty()) {
        EmptyDashboardState(
            isDark = isDark,
            onAddSubscription = onAddSubscription,
            onLoadDemoData = onLoadDemoData,
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Aurora Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RecurraPalette.heroGradient(isDark)),
                ) {
                    AmbientOrbsBackdrop(
                        primaryGlow = RecurraPalette.Emerald,
                        secondaryGlow = RecurraPalette.Violet,
                        modifier = Modifier.matchParentSize(),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "MONTHLY RECURRING BURN",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.72f),
                                )
                                AnimatedCurrencyText(
                                    amount = summary.monthlyTotal,
                                    currencyCode = base,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    text = "${summary.activeCount} active · ${summary.pausedCount} paused · ${summary.activeWarrantiesCount} warranties protected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.78f),
                                )
                            }
                            if (isWide) {
                                Button(
                                    onClick = onAddSubscription,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RecurraPalette.Emerald,
                                        contentColor = Color(0xFF022C22),
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Subscription", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Glass metric pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HeroPill(
                                label = "Yearly Projection",
                                value = FinanceCalculator.formatCurrency(summary.yearlyTotal, base),
                                icon = Icons.Rounded.TrendingUp,
                                tint = RecurraPalette.Emerald,
                                modifier = Modifier.weight(1f),
                            )
                            HeroPill(
                                label = "Due Next 30d",
                                value = FinanceCalculator.formatCurrency(summary.dueNext30DaysTotal, base),
                                icon = Icons.Rounded.EventRepeat,
                                tint = RecurraPalette.Cyan,
                                modifier = Modifier.weight(1f),
                            )
                            HeroPill(
                                label = if (summary.trialCount > 0) "Active Trials" else "Warranties",
                                value = if (summary.trialCount > 0) "${summary.trialCount} ending" else "${summary.activeWarrantiesCount} active",
                                icon = if (summary.trialCount > 0) Icons.Rounded.WarningAmber else Icons.Rounded.Shield,
                                tint = if (summary.trialCount > 0) RecurraPalette.Amber else RecurraPalette.Violet,
                                modifier = Modifier.weight(1f),
                            )
                        }
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

@Composable
private fun HeroPill(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(tint.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
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
        shape = RoundedCornerShape(16.dp),
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
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
) {
    val urgencyColor = when {
        item.isTrialEnding -> RecurraPalette.Amber
        item.daysUntilDue <= 0 -> RecurraPalette.Rose
        item.daysUntilDue <= 5 -> RecurraPalette.Amber
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(category = item.subscription.category, size = 38.dp, iconSize = 19.dp)
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
                            color = RecurraPalette.Amber.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = "TRIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = RecurraPalette.Amber,
                                fontWeight = FontWeight.Bold,
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
                Surface(
                    color = urgencyColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = dueBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = urgencyColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            TextButton(onClick = onMarkPaid, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Mark paid", tint = RecurraPalette.Emerald, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(
    isDark: Boolean,
    onAddSubscription: () -> Unit,
    onLoadDemoData: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(0.95f),
        ) {
            Box(modifier = Modifier.background(RecurraPalette.heroGradient(isDark))) {
                AmbientOrbsBackdrop(
                    primaryGlow = RecurraPalette.Emerald,
                    secondaryGlow = RecurraPalette.Violet,
                    modifier = Modifier.matchParentSize(),
                )
                Column(
                    modifier = Modifier.padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = RecurraPalette.Emerald,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                    Text(
                        text = "Welcome to Recurra",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                    )
                    Text(
                        text = "Track subscriptions, recurring bills, free trials, and product warranties 100% offline. Your financial data never leaves your device.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onAddSubscription,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RecurraPalette.Emerald,
                                contentColor = Color(0xFF022C22),
                            ),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add First Subscription", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onLoadDemoData,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        ) {
                            Text("Explore Sample Data")
                        }
                    }
                }
            }
        }
    }
}
