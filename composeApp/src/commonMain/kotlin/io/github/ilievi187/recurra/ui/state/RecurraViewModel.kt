package io.github.ilievi187.recurra.ui.state

import io.github.ilievi187.recurra.data.DocumentScanner
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.data.VaultStorage
import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.DashboardSummary
import io.github.ilievi187.recurra.domain.DocumentParser
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.MonthlyProjection
import io.github.ilievi187.recurra.domain.RecurraVault
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import io.github.ilievi187.recurra.domain.SubscriptionStatus
import io.github.ilievi187.recurra.domain.WarrantyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppDestination(val label: String) {
    DASHBOARD("Dashboard"),
    SUBSCRIPTIONS("Subscriptions"),
    CALENDAR("Calendar"),
    WARRANTIES("Warranties"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings"),
}

enum class SubscriptionSort(val label: String) {
    NEXT_RENEWAL("Next renewal"),
    AMOUNT_DESC("Cost (high → low)"),
    NAME_ASC("Name (A → Z)"),
}

data class RecurraUiState(
    val vault: RecurraVault = RecurraVault(),
    val summary: DashboardSummary = FinanceCalculator.computeDashboardSummary(RecurraVault()),
    val projections: List<MonthlyProjection> = FinanceCalculator.project12Months(RecurraVault()),
    val destination: AppDestination = AppDestination.DASHBOARD,
    val searchQuery: String = "",
    val selectedCategory: SubscriptionCategory? = null,
    val selectedStatus: SubscriptionStatus? = SubscriptionStatus.ACTIVE,
    val sortBy: SubscriptionSort = SubscriptionSort.NEXT_RENEWAL,
    val showSubscriptionDialog: Boolean = false,
    val editingSubscription: Subscription? = null,
    val showWarrantyDialog: Boolean = false,
    val editingWarranty: WarrantyItem? = null,
    val statusMessage: String? = null,
    val exportFiles: List<String> = emptyList(),
)

class RecurraViewModel(
    private val repository: VaultRepository,
) {
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<RecurraUiState> = _uiState.asStateFlow()

    private fun buildInitialState(): RecurraUiState {
        val vault = repository.loadVault()
        return RecurraUiState(
            vault = vault,
            summary = FinanceCalculator.computeDashboardSummary(vault),
            projections = FinanceCalculator.project12Months(vault),
            exportFiles = runCatching { VaultStorage.listExportFiles() }.getOrDefault(emptyList()),
        )
    }

    private fun commitVault(newVault: RecurraVault, message: String? = null) {
        _uiState.update { state ->
            state.copy(
                vault = newVault,
                summary = FinanceCalculator.computeDashboardSummary(newVault),
                projections = FinanceCalculator.project12Months(newVault),
                statusMessage = message ?: state.statusMessage,
                exportFiles = runCatching { VaultStorage.listExportFiles() }.getOrDefault(state.exportFiles),
            )
        }
    }

    fun selectDestination(destination: AppDestination) {
        _uiState.update { it.copy(destination = destination) }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun filterByCategory(category: SubscriptionCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun filterByStatus(status: SubscriptionStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun updateSort(sort: SubscriptionSort) {
        _uiState.update { it.copy(sortBy = sort) }
    }

    fun openAddSubscriptionDialog() {
        _uiState.update { it.copy(showSubscriptionDialog = true, editingSubscription = null) }
    }

    fun openEditSubscriptionDialog(subscription: Subscription) {
        _uiState.update { it.copy(showSubscriptionDialog = true, editingSubscription = subscription) }
    }

    fun closeSubscriptionDialog() {
        _uiState.update { it.copy(showSubscriptionDialog = false, editingSubscription = null) }
    }

    fun saveSubscription(subscription: Subscription) {
        val isNew = subscription.id.isBlank()
        val updated = repository.upsertSubscription(_uiState.value.vault, subscription)
        _uiState.update { it.copy(showSubscriptionDialog = false, editingSubscription = null) }
        commitVault(updated, if (isNew) "Added ${subscription.name}" else "Updated ${subscription.name}")
    }

    fun deleteSubscription(id: String) {
        val name = _uiState.value.vault.subscriptions.firstOrNull { it.id == id }?.name ?: "Subscription"
        val updated = repository.deleteSubscription(_uiState.value.vault, id)
        commitVault(updated, "Deleted $name")
    }

    fun toggleSubscriptionStatus(id: String) {
        val updated = repository.toggleSubscriptionStatus(_uiState.value.vault, id)
        commitVault(updated, "Updated subscription status")
    }

    fun markSubscriptionPaid(id: String) {
        val name = _uiState.value.vault.subscriptions.firstOrNull { it.id == id }?.name ?: "Subscription"
        val updated = repository.markSubscriptionPaid(_uiState.value.vault, id)
        commitVault(updated, "Recorded payment & advanced cycle for $name")
    }

    fun openAddWarrantyDialog() {
        _uiState.update { it.copy(showWarrantyDialog = true, editingWarranty = null) }
    }

    fun openEditWarrantyDialog(item: WarrantyItem) {
        _uiState.update { it.copy(showWarrantyDialog = true, editingWarranty = item) }
    }

    fun closeWarrantyDialog() {
        _uiState.update { it.copy(showWarrantyDialog = false, editingWarranty = null) }
    }

    fun saveWarranty(item: WarrantyItem) {
        val isNew = item.id.isBlank()
        val updated = repository.upsertWarranty(_uiState.value.vault, item)
        _uiState.update { it.copy(showWarrantyDialog = false, editingWarranty = null) }
        commitVault(updated, if (isNew) "Added warranty for ${item.productName}" else "Updated ${item.productName}")
    }

    fun deleteWarranty(id: String) {
        val updated = repository.deleteWarranty(_uiState.value.vault, id)
        commitVault(updated, "Deleted warranty item")
    }

    fun updateSettings(settings: AppSettings) {
        val updated = repository.updateSettings(_uiState.value.vault, settings)
        commitVault(updated, "Settings saved")
    }

    fun loadDemoData() {
        val updated = repository.loadDemoData(_uiState.value.vault)
        commitVault(updated, "Loaded sample subscriptions & warranties")
    }

    fun clearAllData() {
        val updated = repository.clearAllData(_uiState.value.vault)
        commitVault(updated, "Cleared all local records")
    }

    fun exportCsvToDisk() {
        runCatching {
            val csv = repository.exportCsv(_uiState.value.vault)
            val fileName = "recurra-subscriptions-${FinanceCalculator.today()}.csv"
            val path = VaultStorage.writeExportFile(fileName, csv)
            commitVault(_uiState.value.vault, "Exported CSV to $path")
        }.onFailure { err ->
            showMessage("CSV export failed: ${err.message}")
        }
    }

    fun importCsvFromPath(path: String) {
        runCatching {
            val raw = VaultStorage.readExportFile(path)
            val (updated, count) = repository.importCsv(_uiState.value.vault, raw)
            commitVault(updated, "Imported $count subscription(s) from CSV")
        }.onFailure { err ->
            showMessage("CSV import failed: ${err.message}")
        }
    }

    fun exportEncryptedBackup(passphrase: String) {
        runCatching {
            val encrypted = repository.exportEncryptedBackup(_uiState.value.vault, passphrase)
            val fileName = "recurra-backup-${FinanceCalculator.today()}.recurra.enc"
            val path = VaultStorage.writeExportFile(fileName, encrypted)
            commitVault(_uiState.value.vault, "Encrypted backup saved to $path")
        }.onFailure { err ->
            showMessage("Backup failed: ${err.message}")
        }
    }

    fun restoreEncryptedBackup(filePath: String, passphrase: String) {
        runCatching {
            val payload = VaultStorage.readExportFile(filePath)
            val restored = repository.importEncryptedBackup(payload, passphrase)
            commitVault(restored, "Restored ${restored.subscriptions.size} subscriptions & ${restored.warranties.size} warranties")
        }.onFailure { err ->
            showMessage("Restore failed (check passphrase/file): ${err.message}")
        }
    }

    fun openExternalUrl(url: String) {
        if (!VaultStorage.openExternalUrl(url)) {
            showMessage("Could not open link: $url")
        }
    }

    fun openAttachment(filePath: String) {
        if (!DocumentScanner.openAttachment(filePath)) {
            showMessage("Could not open attachment: $filePath")
        }
    }

    fun scanDocumentForWarranty() {
        showMessage("Select a receipt, invoice, or warranty file (JPG, PNG, or PDF)...")
        DocumentScanner.pickAndScanDocument { outcome ->
            applyWarrantyScanOutcome(outcome)
        }
    }

    fun captureCameraForWarranty() {
        showMessage("Opening camera to scan receipt or warranty...")
        DocumentScanner.captureFromCamera { outcome ->
            applyWarrantyScanOutcome(outcome)
        }
    }

    private fun applyWarrantyScanOutcome(outcome: io.github.ilievi187.recurra.data.ScanOutcome) {
        if (outcome.attachmentPath.isBlank() && outcome.error != null) {
            showMessage(outcome.error)
            return
        }
        val base = _uiState.value.vault.settings.baseCurrency
        val parsed = if (outcome.rawText.isNotBlank()) {
            DocumentParser.parse(outcome.rawText, defaultCurrency = base)
        } else {
            null
        }
        val today = FinanceCalculator.today()
        val prefilled = WarrantyItem(
            id = "",
            productName = parsed?.title.orEmpty(),
            brand = "",
            purchaseDate = parsed?.purchaseDate ?: today.toString(),
            expiryDate = parsed?.expiryDate ?: today.toString(),
            purchasePrice = parsed?.amount ?: 0.0,
            currency = parsed?.currency ?: base,
            vendor = parsed?.vendor.orEmpty(),
            serialNumber = parsed?.serialNumber.orEmpty(),
            receiptReference = parsed?.invoiceNumber.orEmpty(),
            notes = if (parsed != null) "Auto-scanned (${outcome.engineNote})" else outcome.engineNote,
            attachmentPath = outcome.attachmentPath,
        )
        _uiState.update {
            it.copy(
                showWarrantyDialog = true,
                editingWarranty = prefilled,
                statusMessage = outcome.error ?: "Scanned! ${parsed?.summary.orEmpty()}",
            )
        }
    }

    fun scanDocumentForSubscription() {
        showMessage("Select a bill or invoice file (JPG, PNG, or PDF)...")
        DocumentScanner.pickAndScanDocument { outcome ->
            applySubscriptionScanOutcome(outcome)
        }
    }

    fun captureCameraForSubscription() {
        showMessage("Opening camera to scan bill or invoice...")
        DocumentScanner.captureFromCamera { outcome ->
            applySubscriptionScanOutcome(outcome)
        }
    }

    private fun applySubscriptionScanOutcome(outcome: io.github.ilievi187.recurra.data.ScanOutcome) {
        if (outcome.attachmentPath.isBlank() && outcome.error != null) {
            showMessage(outcome.error)
            return
        }
        val base = _uiState.value.vault.settings.baseCurrency
        val parsed = if (outcome.rawText.isNotBlank()) {
            DocumentParser.parse(outcome.rawText, defaultCurrency = base)
        } else {
            null
        }
        val nextDate = parsed?.let {
            FinanceCalculator.parseDateOrNull(it.purchaseDate)?.let { d ->
                FinanceCalculator.advanceDateByCycle(d, it.billingCycle)
            }
        } ?: FinanceCalculator.today()

        val prefilled = Subscription(
            id = "",
            name = parsed?.title?.ifBlank { parsed.vendor }.orEmpty(),
            amount = parsed?.amount ?: 0.0,
            currency = parsed?.currency ?: base,
            cycle = parsed?.billingCycle ?: io.github.ilievi187.recurra.domain.BillingCycle.MONTHLY,
            nextRenewalDate = nextDate.toString(),
            notes = buildString {
                if (parsed != null) {
                    append("Scanned (${outcome.engineNote})")
                    if (parsed.invoiceNumber.isNotBlank()) append(" · Ref ${parsed.invoiceNumber}")
                } else {
                    append(outcome.engineNote)
                }
            },
        )
        _uiState.update {
            it.copy(
                showSubscriptionDialog = true,
                editingSubscription = prefilled,
                statusMessage = outcome.error ?: "Scanned! ${parsed?.summary.orEmpty()}",
            )
        }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
