package io.github.ilievi187.recurra.data

import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.CsvCodec
import io.github.ilievi187.recurra.domain.DemoData
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.PaymentRecord
import io.github.ilievi187.recurra.domain.RecurraVault
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionStatus
import io.github.ilievi187.recurra.domain.WarrantyItem
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface VaultPersistence {
    fun read(): String?
    fun write(json: String)
}

object DefaultVaultPersistence : VaultPersistence {
    override fun read(): String? = VaultStorage.readVaultJson()
    override fun write(json: String) = VaultStorage.writeVaultJson(json)
}

class VaultRepository(
    private val persistence: VaultPersistence = DefaultVaultPersistence,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadVault(): RecurraVault {
        val raw = persistence.read() ?: return RecurraVault()
        return runCatching {
            json.decodeFromString<RecurraVault>(raw)
        }.getOrElse {
            RecurraVault()
        }
    }

    fun saveVault(vault: RecurraVault) {
        persistence.write(json.encodeToString(vault))
    }

    fun upsertSubscription(vault: RecurraVault, subscription: Subscription): RecurraVault {
        val normalized = if (subscription.id.isBlank()) {
            subscription.copy(
                id = newId("sub"),
                createdAt = subscription.createdAt.ifBlank { FinanceCalculator.today().toString() },
            )
        } else {
            subscription
        }
        val exists = vault.subscriptions.any { it.id == normalized.id }
        val updatedList = if (exists) {
            vault.subscriptions.map { if (it.id == normalized.id) normalized else it }
        } else {
            vault.subscriptions + normalized
        }
        return vault.copy(subscriptions = updatedList).also(::saveVault)
    }

    fun deleteSubscription(vault: RecurraVault, id: String): RecurraVault =
        vault.copy(subscriptions = vault.subscriptions.filterNot { it.id == id }).also(::saveVault)

    fun toggleSubscriptionStatus(vault: RecurraVault, id: String): RecurraVault {
        val updated = vault.subscriptions.map { sub ->
            if (sub.id == id) {
                val nextStatus = when (sub.status) {
                    SubscriptionStatus.ACTIVE -> SubscriptionStatus.PAUSED
                    SubscriptionStatus.PAUSED -> SubscriptionStatus.ACTIVE
                    SubscriptionStatus.ARCHIVED -> SubscriptionStatus.ACTIVE
                }
                sub.copy(status = nextStatus)
            } else {
                sub
            }
        }
        return vault.copy(subscriptions = updated).also(::saveVault)
    }

    fun markSubscriptionPaid(vault: RecurraVault, id: String, note: String = "Marked paid"): RecurraVault {
        val target = vault.subscriptions.firstOrNull { it.id == id } ?: return vault
        val currentDue = FinanceCalculator.effectiveDueDate(target) ?: FinanceCalculator.today()
        val nextDue = FinanceCalculator.advanceDateByCycle(currentDue, target.cycle, target.customCycleDays)

        val updatedSub = target.copy(
            nextRenewalDate = nextDue.toString(),
            isFreeTrial = false,
            trialEndDate = null,
        )
        val payment = PaymentRecord(
            id = newId("pay"),
            subscriptionId = target.id,
            subscriptionName = target.name,
            amount = target.amount,
            currency = target.currency,
            paidDate = FinanceCalculator.today().toString(),
            note = note,
        )
        return vault.copy(
            subscriptions = vault.subscriptions.map { if (it.id == id) updatedSub else it },
            paymentHistory = listOf(payment) + vault.paymentHistory,
        ).also(::saveVault)
    }

    fun upsertWarranty(vault: RecurraVault, item: WarrantyItem): RecurraVault {
        val normalized = if (item.id.isBlank()) item.copy(id = newId("war")) else item
        val exists = vault.warranties.any { it.id == normalized.id }
        val updatedList = if (exists) {
            vault.warranties.map { if (it.id == normalized.id) normalized else it }
        } else {
            vault.warranties + normalized
        }
        return vault.copy(warranties = updatedList).also(::saveVault)
    }

    fun deleteWarranty(vault: RecurraVault, id: String): RecurraVault =
        vault.copy(warranties = vault.warranties.filterNot { it.id == id }).also(::saveVault)

    fun updateSettings(vault: RecurraVault, settings: AppSettings): RecurraVault =
        vault.copy(settings = settings).also(::saveVault)

    fun loadDemoData(vault: RecurraVault): RecurraVault {
        val sample = DemoData.sampleVault().copy(settings = vault.settings)
        return sample.also(::saveVault)
    }

    fun clearAllData(vault: RecurraVault): RecurraVault =
        RecurraVault(settings = vault.settings).also(::saveVault)

    fun exportEncryptedBackup(vault: RecurraVault, passphrase: String): String {
        require(passphrase.length >= 6) { "Passphrase must be at least 6 characters." }
        val serialized = json.encodeToString(vault)
        return VaultCrypto.encrypt(serialized, passphrase)
    }

    fun importEncryptedBackup(encryptedPayload: String, passphrase: String): RecurraVault {
        val decrypted = VaultCrypto.decrypt(encryptedPayload, passphrase)
        val restored = json.decodeFromString<RecurraVault>(decrypted)
        saveVault(restored)
        return restored
    }

    fun exportCsv(vault: RecurraVault): String =
        CsvCodec.exportSubscriptions(vault.subscriptions)

    fun importCsv(vault: RecurraVault, csvContent: String): Pair<RecurraVault, Int> {
        val imported = CsvCodec.importSubscriptions(csvContent)
        if (imported.isEmpty()) return vault to 0
        val existingIds = vault.subscriptions.map { it.id }.toSet()
        val uniqueNew = imported.map { sub ->
            if (sub.id in existingIds) sub.copy(id = newId("sub")) else sub
        }
        val merged = vault.copy(subscriptions = vault.subscriptions + uniqueNew)
        saveVault(merged)
        return merged to uniqueNew.size
    }

    private fun newId(prefix: String): String {
        val ms = Clock.System.now().toEpochMilliseconds()
        val rand = (1000..9999).random()
        return "$prefix-$ms-$rand"
    }
}
