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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.UploadFile
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
import io.github.ilievi187.recurra.ui.components.AnimatedCurrencyText
import io.github.ilievi187.recurra.ui.components.BotanicalIllustrationCanvas
import io.github.ilievi187.recurra.ui.components.CategoryAvatar
import io.github.ilievi187.recurra.ui.components.CategoryDonutChart
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import io.github.ilievi187.recurra.ui.theme.RecurraPalette

@Composable
fun DashboardScreen(
    state: RecurraUiState,
    isWide: Boolean,
    onAddSubscription: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadFile: () -> Unit,
    onLoadDemoData: () -> Unit,
    onClearDemoData: () -> Unit,
    onMarkPaid: (String) -> Unit,
    onEditSubscription: (String) -> Unit,
    onOpenSubscriptions: () -> Unit,
) {
    val summary = state.summary
    val base = state.vault.settings.baseCurrency
    val today = FinanceCalculator.today()
    val isDark = when (state.vault.settings.themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val isDemoMode = state.vault.subscriptions.any { it.id.startsWith("demo-") } ||
        state.vault.warranties.any { it.id.startsWith("war-") }

    if (state.vault.subscriptions.isEmpty() && state.vault.warranties.isEmpty()) {
        EmptyDashboardState(
            monthNumber = today.monthNumber,
            isDark = isDark,
            onAddSubscription = onAddSubscription,
            onTakePhoto = onTakePhoto,
            onUploadFile = onUploadFile,
            onLoadDemoData = onLoadDemoData,
        )
        return
    }

    val heroTextColor = if (isDark) Color(0xFFF3F2EC) else Color(0xFF1A2E23)
    val heroSubColor = if (isDark) Color(0xFFC5D0C8) else Color(0xFF42594C)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Friendly 1-click exit banner whenever Sample Demo Data is active
        if (isDemoMode) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, RecurraPalette.WarmYellow.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                            Column {
                                Text(
                                    text = "You're viewing sample demo data",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Text(
                                    text = "Ready to track your real bills? Clear the demo anytime with one click.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = onClearDemoData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clear Demo Data", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        // Warm Botanical Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RecurraPalette.heroGradient(isDark)),
                ) {
                    BotanicalIllustrationCanvas(
                        monthNumber = today.monthNumber,
                        isDark = isDark,
                        modifier = Modifier.matchParentSize(),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Your Monthly Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = heroSubColor,
                                )
                                AnimatedCurrencyText(
                                    amount = summary.monthlyTotal,
                                    currencyCode = base,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = heroTextColor,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    text = "${summary.activeCount} active bills · ${summary.pausedCount} paused · ${summary.activeWarrantiesCount} warranties saved",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = heroSubColor,
                                )
                            }
                            if (isWide) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onTakePhoto,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isDark) Color(0xFF1A241E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.85f),
                                        ),
                                    ) {
                                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Take Photo", fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = onUploadFile,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isDark) Color(0xFF1A241E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.85f),
                                        ),
                                    ) {
                                        Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Upload File", fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = onAddSubscription,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Add Bill", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Soft warm metric cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HeroPill(
                                label = "Yearly Total",
                                value = FinanceCalculator.formatCurrency(summary.yearlyTotal, base),
                                icon = Icons.Rounded.TrendingUp,
                                tint = RecurraPalette.Sage,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                            )
                            HeroPill(
                                label = "Due Next 30 Days",
                                value = FinanceCalculator.formatCurrency(summary.dueNext30DaysTotal, base),
                                icon = Icons.Rounded.EventRepeat,
                                tint = RecurraPalette.WarmYellow,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                            )
                            HeroPill(
                                label = if (summary.trialCount > 0) "Free Trials" else "Warranties",
                                value = if (summary.trialCount > 0) "${summary.trialCount} ending soon" else "${summary.activeWarrantiesCount} protected",
                                icon = if (summary.trialCount > 0) Icons.Rounded.WarningAmber else Icons.Rounded.Shield,
                                tint = if (summary.trialCount > 0) RecurraPalette.WarmPeach else RecurraPalette.SageLight,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        // Main content: Upcoming Bills + Category Breakdown
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
                        title = "Where Your Money Goes",
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
                    SectionCard(title = "Where Your Money Goes") {
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
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF1A241E).copy(alpha = 0.80f) else Color.White.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFDCE7DF)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
        title = "Upcoming Bills",
        modifier = modifier,
        action = {
            TextButton(onClick = onViewAll) { Text("See all") }
        },
    ) {
        if (upcoming.isEmpty()) {
            Text(
                text = "Nothing due soon — you're all caught up!",
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
        item.isTrialEnding -> RecurraPalette.WarmPeach
        item.daysUntilDue <= 0 -> RecurraPalette.SoftCoral
        item.daysUntilDue <= 5 -> RecurraPalette.WarmYellow
        else -> MaterialTheme.colorScheme.primary
    }
    val dueBadge = when {
        item.daysUntilDue < 0 -> "Overdue ${-item.daysUntilDue}d"
        item.daysUntilDue == 0 -> "Due today"
        item.daysUntilDue == 1 -> "Tomorrow"
        else -> "In ${item.daysUntilDue} days"
    }

    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(category = item.subscription.category, size = 40.dp, iconSize = 20.dp)
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
                            color = RecurraPalette.ButterYellow,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "FREE TRIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
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
                    color = urgencyColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = dueBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = urgencyColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            TextButton(onClick = onMarkPaid, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Mark paid", tint = RecurraPalette.Sage, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(
    monthNumber: Int,
    isDark: Boolean,
    onAddSubscription: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadFile: () -> Unit,
    onLoadDemoData: () -> Unit,
) {
    val textColor = if (isDark) Color(0xFFF3F2EC) else Color(0xFF1A2E23)
    val subColor = if (isDark) Color(0xFFC5D0C8) else Color(0xFF42594C)

    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(0.95f),
        ) {
            Box(modifier = Modifier.background(RecurraPalette.heroGradient(isDark))) {
                BotanicalIllustrationCanvas(
                    monthNumber = monthNumber,
                    isDark = isDark,
                    modifier = Modifier.matchParentSize(),
                )
                Column(
                    modifier = Modifier.padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFlorist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                    Text(
                        text = "Welcome to Recurra",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                    )
                    Text(
                        text = "A calm, private home for your subscriptions, bills, free trials, and product warranties. Take a photo with your camera, upload a PDF/image, or add one manually.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = subColor,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onTakePhoto,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Take Photo", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onUploadFile,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) Color(0xFF1A241E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.85f),
                            ),
                        ) {
                            Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Upload PDF / Image")
                        }
                        OutlinedButton(
                            onClick = onAddSubscription,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) Color(0xFF1A241E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.85f),
                            ),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Manually")
                        }
                    }
                    TextButton(onClick = onLoadDemoData) {
                        Text("Or try Sample Demo Data first", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
