package io.github.ilievi187.recurra.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.ui.components.SubscriptionDialog
import io.github.ilievi187.recurra.ui.components.WarrantyDialog
import io.github.ilievi187.recurra.ui.screens.AnalyticsScreen
import io.github.ilievi187.recurra.ui.screens.CalendarScreen
import io.github.ilievi187.recurra.ui.screens.DashboardScreen
import io.github.ilievi187.recurra.ui.screens.SettingsScreen
import io.github.ilievi187.recurra.ui.screens.SubscriptionsScreen
import io.github.ilievi187.recurra.ui.screens.WarrantiesScreen
import io.github.ilievi187.recurra.ui.state.AppDestination
import io.github.ilievi187.recurra.ui.state.RecurraViewModel
import io.github.ilievi187.recurra.ui.theme.RecurraPalette
import io.github.ilievi187.recurra.ui.theme.RecurraTheme

@Composable
fun RecurraApp(
    viewModel: RecurraViewModel,
    isDesktop: Boolean,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    RecurraTheme(preference = state.vault.settings.themePreference) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val isWide = isDesktop || maxWidth >= 780.dp

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (!isWide) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                AppDestination.entries.forEach { dest ->
                                    NavigationBarItem(
                                        selected = state.destination == dest,
                                        onClick = { viewModel.selectDestination(dest) },
                                        icon = { Icon(iconFor(dest), contentDescription = dest.label) },
                                        label = { Text(shortLabel(dest)) },
                                    )
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (!isWide && (state.destination == AppDestination.DASHBOARD || state.destination == AppDestination.SUBSCRIPTIONS)) {
                        FloatingActionButton(
                            onClick = { viewModel.openAddSubscriptionDialog() },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Subscription")
                        }
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (isWide) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Column(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(RecurraPalette.Sage, RecurraPalette.WarmYellow),
                                                ),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "R",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Recurra",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            Spacer(Modifier.height(8.dp))
                            AppDestination.entries.forEach { dest ->
                                NavigationRailItem(
                                    selected = state.destination == dest,
                                    onClick = { viewModel.selectDestination(dest) },
                                    icon = { Icon(iconFor(dest), contentDescription = dest.label) },
                                    label = { Text(shortLabel(dest)) },
                                )
                            }
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        AnimatedContent(
                            targetState = state.destination,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 }) togetherWith
                                    fadeOut(tween(150))
                            },
                            label = "screenTransition",
                        ) { destination ->
                            when (destination) {
                                AppDestination.DASHBOARD -> DashboardScreen(
                                    state = state,
                                    isWide = isWide,
                                    onAddSubscription = viewModel::openAddSubscriptionDialog,
                                    onLoadDemoData = viewModel::loadDemoData,
                                    onMarkPaid = viewModel::markSubscriptionPaid,
                                    onEditSubscription = { id ->
                                        state.vault.subscriptions.firstOrNull { it.id == id }
                                            ?.let(viewModel::openEditSubscriptionDialog)
                                    },
                                    onOpenSubscriptions = { viewModel.selectDestination(AppDestination.SUBSCRIPTIONS) },
                                )

                                AppDestination.SUBSCRIPTIONS -> SubscriptionsScreen(
                                    state = state,
                                    isWide = isWide,
                                    onSearchChange = viewModel::updateSearch,
                                    onStatusFilter = viewModel::filterByStatus,
                                    onCategoryFilter = viewModel::filterByCategory,
                                    onSortChange = viewModel::updateSort,
                                    onAdd = viewModel::openAddSubscriptionDialog,
                                    onScanInvoice = viewModel::scanDocumentForSubscription,
                                    onEdit = viewModel::openEditSubscriptionDialog,
                                    onMarkPaid = viewModel::markSubscriptionPaid,
                                    onToggleStatus = viewModel::toggleSubscriptionStatus,
                                    onDelete = viewModel::deleteSubscription,
                                    onOpenUrl = viewModel::openExternalUrl,
                                )

                                AppDestination.CALENDAR -> CalendarScreen(
                                    state = state,
                                    onEditSubscription = viewModel::openEditSubscriptionDialog,
                                    onMarkPaid = viewModel::markSubscriptionPaid,
                                )

                                AppDestination.WARRANTIES -> WarrantiesScreen(
                                    state = state,
                                    onAdd = viewModel::openAddWarrantyDialog,
                                    onScanReceipt = viewModel::scanDocumentForWarranty,
                                    onOpenAttachment = viewModel::openAttachment,
                                    onEdit = viewModel::openEditWarrantyDialog,
                                    onDelete = viewModel::deleteWarranty,
                                )

                                AppDestination.ANALYTICS -> AnalyticsScreen(state = state)

                                AppDestination.SETTINGS -> SettingsScreen(
                                    state = state,
                                    onUpdateSettings = viewModel::updateSettings,
                                    onExportCsv = viewModel::exportCsvToDisk,
                                    onImportCsv = viewModel::importCsvFromPath,
                                    onExportEncryptedBackup = viewModel::exportEncryptedBackup,
                                    onRestoreEncryptedBackup = viewModel::restoreEncryptedBackup,
                                    onLoadDemoData = viewModel::loadDemoData,
                                    onClearAllData = viewModel::clearAllData,
                                    onOpenUrl = viewModel::openExternalUrl,
                                )
                            }
                        }
                    }
                }
            }

            if (state.showSubscriptionDialog) {
                SubscriptionDialog(
                    initial = state.editingSubscription,
                    defaultCurrency = state.vault.settings.baseCurrency,
                    onDismiss = viewModel::closeSubscriptionDialog,
                    onSave = viewModel::saveSubscription,
                )
            }

            if (state.showWarrantyDialog) {
                WarrantyDialog(
                    initial = state.editingWarranty,
                    defaultCurrency = state.vault.settings.baseCurrency,
                    onDismiss = viewModel::closeWarrantyDialog,
                    onSave = viewModel::saveWarranty,
                )
            }
        }
    }
}

private fun iconFor(destination: AppDestination): ImageVector = when (destination) {
    AppDestination.DASHBOARD -> Icons.Rounded.Dashboard
    AppDestination.SUBSCRIPTIONS -> Icons.Rounded.EventRepeat
    AppDestination.CALENDAR -> Icons.Rounded.CalendarMonth
    AppDestination.WARRANTIES -> Icons.Rounded.Shield
    AppDestination.ANALYTICS -> Icons.Rounded.BarChart
    AppDestination.SETTINGS -> Icons.Rounded.Settings
}

private fun shortLabel(destination: AppDestination): String = when (destination) {
    AppDestination.DASHBOARD -> "Home"
    AppDestination.SUBSCRIPTIONS -> "Subs"
    AppDestination.CALENDAR -> "Calendar"
    AppDestination.WARRANTIES -> "Warranty"
    AppDestination.ANALYTICS -> "Stats"
    AppDestination.SETTINGS -> "Settings"
}
