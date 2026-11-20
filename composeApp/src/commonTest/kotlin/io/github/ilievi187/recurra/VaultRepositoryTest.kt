package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.data.VaultPersistence
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.domain.BillingCycle
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private class InMemoryPersistence : VaultPersistence {
    var raw: String? = null
    override fun read(): String? = raw
    override fun write(json: String) {
        raw = json
    }
}

class VaultRepositoryTest {

    @Test
    fun crudAndMarkPaidPersistCorrectly() {
        val mem = InMemoryPersistence()
        val repo = VaultRepository(mem)

        var vault = repo.loadVault()
        assertEquals(0, vault.subscriptions.size)

        vault = repo.upsertSubscription(
            vault,
            Subscription(
                id = "",
                name = "Proton Unlimited",
                amount = 9.99,
                currency = "EUR",
                cycle = BillingCycle.MONTHLY,
                nextRenewalDate = "2026-11-25",
                isFreeTrial = true,
                trialEndDate = "2026-11-25",
            ),
        )
        assertEquals(1, vault.subscriptions.size)
        val id = vault.subscriptions.first().id

        // Mark paid should advance date by 1 month, clear trial flag, and log payment
        vault = repo.markSubscriptionPaid(vault, id)
        val updated = vault.subscriptions.first()
        assertEquals("2026-12-25", updated.nextRenewalDate)
        assertFalse(updated.isFreeTrial)
        assertEquals(1, vault.paymentHistory.size)

        // Toggle status to PAUSED
        vault = repo.toggleSubscriptionStatus(vault, id)
        assertEquals(SubscriptionStatus.PAUSED, vault.subscriptions.first().status)

        // Reload from serialized JSON
        val reloaded = repo.loadVault()
        assertEquals(1, reloaded.subscriptions.size)
        assertEquals(1, reloaded.paymentHistory.size)

        // Delete
        vault = repo.deleteSubscription(reloaded, id)
        assertEquals(0, vault.subscriptions.size)
    }
}
