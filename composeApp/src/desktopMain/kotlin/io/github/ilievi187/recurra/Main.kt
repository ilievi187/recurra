package io.github.ilievi187.recurra

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.ilievi187.recurra.data.VaultCrypto
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.data.VaultStorage
import io.github.ilievi187.recurra.domain.DemoData
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.ui.RecurraApp
import io.github.ilievi187.recurra.ui.state.RecurraViewModel
import java.awt.Dimension
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    if ("--smoke-test" in args) {
        runHeadlessSmokeTest()
        return
    }

    val repository = VaultRepository()
    val viewModel = RecurraViewModel(repository)

    // Best-effort Linux desktop notification for renewals due within 3 days
    notifyUpcomingRenewalsOnLinux(viewModel)

    application {
        val windowState = rememberWindowState(
            width = 1220.dp,
            height = 800.dp,
            position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center),
        )
        val appIcon = remember { loadWindowIcon() }
        Window(
            onCloseRequest = ::exitApplication,
            title = "Recurra — Subscriptions & Warranties",
            icon = appIcon,
            state = windowState,
            onPreviewKeyEvent = { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                    when (event.key) {
                        Key.N -> {
                            viewModel.openAddSubscriptionDialog()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        ) {
            window.minimumSize = Dimension(880, 620)
            RecurraApp(viewModel = viewModel, isDesktop = true)
        }
    }
}

private fun loadWindowIcon(): BitmapPainter? = runCatching {
    val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream("recurra.png")
        ?: return@runCatching null
    stream.use { input ->
        val buffered = ImageIO.read(input) ?: return@runCatching null
        BitmapPainter(buffered.toComposeImageBitmap())
    }
}.getOrNull()

private fun notifyUpcomingRenewalsOnLinux(viewModel: RecurraViewModel) {
    val os = System.getProperty("os.name").lowercase()
    if (!os.contains("linux")) return
    val urgent = viewModel.uiState.value.summary.upcomingRenewals.filter { it.daysUntilDue in 0..3 }
    if (urgent.isEmpty()) return

    val summaryTitle = if (urgent.size == 1) {
        "Recurra: ${urgent.first().subscription.name} due soon"
    } else {
        "Recurra: ${urgent.size} renewals due in the next 3 days"
    }
    val body = urgent.take(3).joinToString("\n") { item ->
        val whenLabel = when (item.daysUntilDue) {
            0 -> "today"
            1 -> "tomorrow"
            else -> "in ${item.daysUntilDue} days"
        }
        "${item.subscription.name} (${FinanceCalculator.formatCurrency(item.subscription.amount, item.subscription.currency)}) — $whenLabel"
    }
    runCatching {
        ProcessBuilder("notify-send", "-a", "Recurra", summaryTitle, body).start()
    }
}

private fun runHeadlessSmokeTest() {
    val sample = DemoData.sampleVault()
    val summary = FinanceCalculator.computeDashboardSummary(sample)
    check(summary.activeCount > 0) { "Expected active subscriptions in smoke test" }
    check(summary.monthlyTotal > 0.0) { "Expected positive monthly total in smoke test" }

    val encrypted = VaultCrypto.encrypt("recurra-smoke-ok", "test-passphrase-123")
    val decrypted = VaultCrypto.decrypt(encrypted, "test-passphrase-123")
    check(decrypted == "recurra-smoke-ok") { "Crypto roundtrip failed" }

    println("RECURRA_SMOKE_TEST_PASSED active=${summary.activeCount} monthly=${"%.2f".format(summary.monthlyTotal)} dataDir=${VaultStorage.vaultDirectoryPath()}")
}
