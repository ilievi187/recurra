package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.domain.BillingCycle
import io.github.ilievi187.recurra.domain.CsvCodec
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvCodecTest {

    @Test
    fun roundTripsSubscriptionsWithCommasAndQuotes() {
        val original = listOf(
            Subscription(
                id = "sub-1",
                name = "Acme, Inc. \"Pro\"",
                amount = 19.99,
                currency = "EUR",
                cycle = BillingCycle.MONTHLY,
                nextRenewalDate = "2026-12-05",
                category = SubscriptionCategory.PRODUCTIVITY,
                paymentMethod = "Visa •••• 1234",
                notes = "Includes \"Team\" add-on, billed monthly",
            ),
        )

        val csv = CsvCodec.exportSubscriptions(original)
        val parsed = CsvCodec.importSubscriptions(csv)

        assertEquals(1, parsed.size)
        assertEquals(original[0].name, parsed[0].name)
        assertEquals(original[0].amount, parsed[0].amount, 0.001)
        assertEquals(original[0].currency, parsed[0].currency)
        assertEquals(original[0].notes, parsed[0].notes)
        assertEquals(original[0].category, parsed[0].category)
    }
}
