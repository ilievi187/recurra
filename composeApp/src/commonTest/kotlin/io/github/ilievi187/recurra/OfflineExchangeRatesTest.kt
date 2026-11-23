package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.data.VaultPersistence
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.DefaultCurrencies
import io.github.ilievi187.recurra.domain.OfflineExchangeRates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OfflineExchangeRatesTest {
    @Test
    fun displaysRatesRelativeToSelectedBaseCurrency() {
        val usd = AppSettings(baseCurrency = "USD")
        assertEquals(1.08, OfflineExchangeRates.rateInBase(usd, "EUR"), 0.000001)
        assertEquals(1.0, OfflineExchangeRates.rateInBase(usd, "USD"), 0.0)

        val eur = usd.copy(baseCurrency = "EUR")
        assertEquals(1.0 / 1.08, OfflineExchangeRates.rateInBase(eur, "USD"), 0.000001)
        assertEquals(1.27 / 1.08, OfflineExchangeRates.rateInBase(eur, "GBP"), 0.000001)
        assertEquals(1.0, OfflineExchangeRates.rateInBase(eur, "EUR"), 0.0)
    }

    @Test
    fun editingNonUsdCurrencyPersistsInUsdAnchoredMap() {
        val settings = AppSettings(baseCurrency = "EUR")
        val updated = OfflineExchangeRates.withRate(settings, "GBP", 1.5)
        assertEquals(1.5, OfflineExchangeRates.rateInBase(updated, "GBP"), 0.000001)
        assertEquals(1.5 * 1.08, updated.ratesToUsd.getValue("GBP"), 0.000001)
        assertEquals(1.0, updated.ratesToUsd.getValue("USD"), 0.0)
        assertEquals(
            OfflineExchangeRates.rateInBase(settings, "USD"),
            OfflineExchangeRates.rateInBase(updated, "USD"),
            0.000001,
        )
    }

    @Test
    fun editingUsdWithNonUsdBasePreservesOtherCrossRates() {
        val settings = AppSettings(baseCurrency = "EUR")
        val originalGbp = OfflineExchangeRates.rateInBase(settings, "GBP")
        val originalBgn = OfflineExchangeRates.rateInBase(settings, "BGN")
        val updated = OfflineExchangeRates.withRate(settings, "USD", 0.8)

        assertEquals(0.8, OfflineExchangeRates.rateInBase(updated, "USD"), 0.000001)
        assertEquals(1.25, updated.ratesToUsd.getValue("EUR"), 0.000001)
        assertEquals(1.0, updated.ratesToUsd.getValue("USD"), 0.0)
        assertEquals(originalGbp, OfflineExchangeRates.rateInBase(updated, "GBP"), 0.000001)
        assertEquals(originalBgn, OfflineExchangeRates.rateInBase(updated, "BGN"), 0.000001)
    }

    @Test
    fun editingRateWithUsdBaseKeepsUnrelatedCurrencies() {
        val settings = AppSettings(baseCurrency = "USD")
        val updated = OfflineExchangeRates.withRate(settings, "EUR", 1.2)
        assertEquals(1.2, updated.ratesToUsd.getValue("EUR"), 0.000001)
        assertEquals(settings.ratesToUsd.getValue("GBP"), updated.ratesToUsd.getValue("GBP"), 0.0)
    }

    @Test
    fun rejectsBadOrUnsupportedRates() {
        val settings = AppSettings(baseCurrency = "EUR")
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach {
            assertFailsWith<IllegalArgumentException> {
                OfflineExchangeRates.withRate(settings, "GBP", it)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            OfflineExchangeRates.withRate(settings, "EUR", 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            OfflineExchangeRates.withRate(settings, "XYZ", 2.0)
        }
        assertFailsWith<IllegalArgumentException> {
            OfflineExchangeRates.withRate(settings, "USD", Double.MIN_VALUE)
        }
    }

    @Test
    fun updatedRatesSurviveOfflineVaultSaveAndReload() {
        val persistence = object : VaultPersistence {
            private var data: String? = null
            override fun read(): String? = data
            override fun write(json: String) { data = json }
        }
        val repository = VaultRepository(persistence)
        val vault = repository.loadVault()
        val updated = OfflineExchangeRates.withRate(
            vault.settings.copy(baseCurrency = "EUR"),
            "GBP",
            1.4,
        )
        repository.updateSettings(vault, updated)
        val reloaded = repository.loadVault()
        assertEquals("EUR", reloaded.settings.baseCurrency)
        assertEquals(1.4, OfflineExchangeRates.rateInBase(reloaded.settings, "GBP"), 0.000001)
        assertEquals(DefaultCurrencies.defaultRatesToUsd.size, reloaded.settings.ratesToUsd.size)
    }
}
